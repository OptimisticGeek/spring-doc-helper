package com.github.optimistic.spring.parse

import com.github.optimistic.spring.constant.FieldType
import com.github.optimistic.spring.constant.RETURN
import com.github.optimistic.spring.constant.VOID
import com.github.optimistic.spring.constant.getFieldType
import com.github.optimistic.spring.ext.*
import com.github.optimistic.spring.model.ClassType
import com.github.optimistic.spring.model.Field
import com.github.optimistic.spring.model.getLeadValue
import com.github.optimistic.spring.model.isDebugOrTests
import com.github.optimistic.spring.model.type.*
import com.github.optimistic.spring.service.getFileUserData
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.impl.java.stubs.index.JavaFullClassNameIndex
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentOfType
import org.apache.commons.lang3.BooleanUtils
import java.lang.Thread.currentThread

class PsiParseClassServiceImpl(val project: Project) : PsiParseClassService {
    val service: PsiParseClassService get() = project.parseService()

    val projectScope: GlobalSearchScope get() = GlobalSearchScope.allScope(project)

    val classCache: JavaFullClassNameIndex get() = JavaFullClassNameIndex.getInstance()

    val track get() = parseThreadTrack.getParseTrack()

    /**
     * 分析类的字段，通过构造函数、复制、getter进行分析
     */
    override fun refreshFields(
        psi: PsiClass, obj: ObjClass, isForce: Boolean?
    ): ClassType {
        if (obj is ClassType) throw RuntimeException("禁止使用ClassType:$obj")
        var root = ClassType(obj)
        takeIf { obj.fields.isNotEmpty() && obj.isInit && isForce != true }.also { obj.isInit = true }
            ?.also { return root }
        if (psi.isInterface || psi.isEnum || !psi.isValid) return root

        obj.run {
            fields.clear()
            this.psi = SmartPointerManager.createPointer<PsiClass>(psi)
            this.author = psi.getAuthor()
            this.remark = psi.getRemark()
            // todo 分析Lombok

            // 字段分析
            psi.allFields.filter { !it.hasModifierProperty("static") && it.hasModifierProperty("private") }
                .mapNotNull { Field(it) }.filter { it.isRequired != null }.let { mergedFields(it) }/*
            // 推过getter获取字段
            psi.methods.mapNotNull { psi.project.parseService().parseBaseClassAtGetter(it) }
                .mapNotNull { psi.findFieldByName(it.first, true) }.map { Field(it) }.let { mergedFields(it) }*/

            // 分析构造函数中的字段，多数情况为map
            psi.constructors.maxByOrNull { it.parameterList.parametersCount }
                ?.let { method -> service.parseParamsBaseClass(method, root, null) }?.let { mergedFields(it.fields) }

            this.refreshTargetFields()
        }
        return root.refresh(true)
    }

    override fun getBaseClass(psi: PsiTypeElement?): BaseClass? = psi?.let { service.getBaseClass(psi.type) }

    override fun getBaseClass(psi: PsiType?): BaseClass? {
        if (psi == null || psi.equalsToText(VOID)) return null
        val qName = psi.canonicalText
        val root = createBaseClass(qName, null) ?: return null
        root.takeIf { it is BasicClass }?.let { return if (psi is PsiArrayType) it.createList() else it }

        when (psi) {
            is PsiArrayType -> service.getBaseClass(psi.componentType)?.createList()

            is PsiClassReferenceType -> {
                root.apply {
                    if (checkValid() || psi.parameters.isEmpty()) return@apply
                    this.init(service.getBaseClass(psi.parameters.lastOrNull()))
                }
            }

            is PsiPrimitiveType -> root

            is PsiWildcardType -> root

            else -> root
        }?.also { return it }
        printAllTrack("获取类失败：$qName  <-[$psi]", qName.length > 1)
        return root
    }


    override fun getBaseClass(qName: String?, psi: PsiClass?): BaseClass? {
        var qName = psi?.qualifiedName ?: qName ?: return null
        qName.takeIf { qName != VOID } ?: return null

        val root = createBaseClass(qName, psi)?.let { it as? ClassType ?: return it } ?: return null

        var tmp: BaseClass? = null
        qNameRegex.findAll(qName).map { it.value.trim() }.filter { !it.endsWith(",") }
            .map { qName -> createBaseClass(qName, null) }.forEachIndexed { index, it ->
                it ?: return root
                when (index) {
                    0 -> root // root自身
                    1 -> root.init(it)
                    else -> tmp?.init(it)
                }
                tmp = it
            }
        return root
    }

    /**
     * 创建对象，并解析字段。
     * 例如：ResultData<List<Integer>>，只会解析ResultData，不会解析List<Integer>
     */
    private fun createBaseClass(qName: String?, psi: PsiClass?): BaseClass? {
        // todo 缺少 int[]数组类型的判断
        var qName = psi?.qualifiedName ?: qName?.firstQualifiedName() ?: return null
        val fieldType = getFieldType(psi, qName).takeIf { it != FieldType.OTHER } ?: return null
        // 基本类型无需处理
        fieldType.baseClass?.let { return it }
        // 尝试从缓存获取对象
        val psi = psi ?: classCache.get(qName, project, projectScope)?.firstOrNull()

        // 引用类型，避免在同一个对象进行操作，每次都会新建一个对象
        return when (fieldType) {
            FieldType.LIST, FieldType.ARRAY -> ListClass(fieldType)
            FieldType.SUBSTITUTE -> SubstituteClass(qName)
            FieldType.MAP -> (psi?.getFileUserData(ObjClassKey) { MapClass(qName = qName) } ?: MapClass(qName = qName))
            FieldType.OBJECT -> (psi?.getFileUserData(ObjClassKey) { ObjClass(qName = qName) }
                ?: ObjClass(qName = qName))

            else -> null
        }?.let { if (psi == null || it !is ObjClass) return ClassType(it) else service.refreshFields(psi, it) }
    }

    /**
     * 任意element识别
     */
    override fun parseBaseClass(psi: PsiElement, from: PsiElement?): BaseClass? = when (psi) {
        is PsiIdentifier -> service.parseBaseClass(psi.parent, from)
        is PsiType -> service.getBaseClass(psi)
        is PsiClass -> service.getBaseClass(psi.qualifiedName, psi)
        is PsiTypeElement -> service.getBaseClass(psi)
        is PsiMethod -> service.parseMethodBaseClass(psi, psi as? PsiMethodCallExpression)
        is PsiVariable -> service.parseVariableBaseClass(psi)
        is PsiNewExpression -> service.parseParamsBaseClass(psi, from as? PsiCallExpression ?: psi)
        is PsiReturnStatement -> service.parseReturnBaseClass(psi)
        is PsiLiteralExpression -> service.parseLiteralBaseClass(psi)
        is PsiMethodCallExpression -> service.parseMethodCallBaseClass(psi)
        is PsiConditionalExpression -> service.parseConditionalBaseClass(psi)
        is PsiJavaCodeReferenceElement -> service.parseJavaCodeRefBaseClass(psi)
        else -> thisLogger().warn("invalidParsePsi: $psi").let { null }
    }

    /**
     * 解析方法形参、实参以及codeBlock
     */
    override fun parseMethodBaseClass(
        psi: PsiMethod, call: PsiCallExpression?
    ): BaseClass? = psi.run {
        if (!isConstructor && returnTypeElement != null && returnTypeElement!!.text != VOID) {
            return service.parseReturnBaseClass(psi, call = call)
        }
        val root = this.containingClass?.let { service.getBaseClass(psi = it) }
        root as? ClassType ?: return root
        service.parseParamsBaseClass(psi, root, call)
    }

    override fun parseReturnBaseClass(
        psi: PsiMethod, call: PsiCallExpression?
    ): BaseClass? = psi.run {
        val responseBody =
            this.returnTypeElement?.let { service.getBaseClass(it) }?.also { it.remark = this.getDocumentTag(RETURN) }
        responseBody as? ClassType ?: return responseBody
        service.putLead(psi, call, responseBody)
        if (isConstructor) return service.parseParamsBaseClass(psi, responseBody, call)
        // 判断泛型是否解析成功
        responseBody.takeIf { it.checkValid() }?.let { return it }
        // 从return语句中分析评分最高的返回值，并于responseBody对比
        var maxScore =
            PsiTreeUtil.findChildrenOfType<PsiReturnStatement>(this.body, PsiReturnStatement::class.java)
                .mapNotNull { service.parseReturnBaseClass(it) as? ClassType }.maxByOrNull { it.getScore() }
                ?: return responseBody
        responseBody.takeIf { responseBody.getScore() > maxScore.getScore() } ?: maxScore
    }

    /**
     * 解析return代码行
     */
    override fun parseReturnBaseClass(
        psi: PsiReturnStatement
    ): BaseClass? = psi.children.getOrNull(2)?.let { service.parseBaseClass(it, psi) }

    /**
     * 解析方法参数
     */
    override fun parseParamsBaseClass(psi: PsiNewExpression, call: PsiCallExpression?): BaseClass? = psi.run {
        val method = resolveConstructor() ?: return null
        return@run service.getBaseClass(type)?.let {
            if (it is ClassType) service.parseParamsBaseClass(method, it, call) else it
        }
    }

    /**
     * 解析methodCall
     */
    override fun parseMethodCallBaseClass(
        psi: PsiMethodCallExpression, root: ClassType?
    ): BaseClass? = psi.resolveMethod()?.let { method -> service.parseReturnBaseClass(method, psi) }

    override fun parseParamsBaseClass(
        psi: PsiMethod, root: ClassType, call: PsiCallExpression?
    ): ClassType? = psi.run {
        service.putLead(psi, call, root)
        // 自身为setter
        takeIf { service.parseBaseClassAtSetter(psi, call, root) == true }?.let { return root }
        // 自身为map.put
        call?.takeIf { service.parseBaseClassAtMapPut(psi, root, call) == true }?.let { return root }
        // 解析代码块，等号赋值、setter
        PsiTreeUtil.findChildrenOfType<PsiExpressionStatement>(this.body, PsiExpressionStatement::class.java).forEach {
            when (val child = it.firstChild) {
                // result.data = new ArrayList<Integer>();
                is PsiAssignmentExpression -> service.parseAssignmentBaseClass(child, root)
                // result.setData(new ArrayList<ResultData<Integer>>());
                is PsiMethodCallExpression -> child.resolveMethod()
                    ?.let { method -> service.parseParamsBaseClass(method, root, child) }

                else -> null
            }
        }
        return root
    }

    /**
     * 添加线索，并且会尝试匹配field
     */
    override fun putLead(
        psi: PsiMethod, call: PsiCallExpression?, root: ClassType
    ) = psi.run {
        if (parameterList.parametersCount == 0) return
        val methodName = this.name
        val callMethodName = call?.resolveMethod()?.name
        parameterList.parameters.forEachIndexed { index, param ->
            // 将形参添加到track
            service.getBaseClass(param.type).run {
                track?.putLeadToMap(param.name, this)
                track?.putLeadToMap("$methodName#$index", this)
            }
            // 将实参添加到track
            val expression = call?.argumentList?.expressions?.getOrNull(index) ?: return@forEachIndexed
            service.parseBaseClass(expression, call)?.run {
                track?.putLeadToMap(param.name, this)
                track?.putLeadToMap(expression.text, this)
                track?.putLeadToMap("$callMethodName#$index", this)
            }
            root.targetFields.map { it.name }.forEach { fieldName ->
                if (param.name == fieldName || expression.text == fieldName || call.text.contains(fieldName)) {
                    track?.putLeadToMap(fieldName, track?.getLead(param.name, call))
                    root.putField(
                        fieldName, track.getLeadValue(param.name)
                    )
                }
            }
        }
    }

    /**
     * 解析getter方法
     */
    override fun parseBaseClassAtGetter(
        psi: PsiMethod
    ): Pair<String, BaseClass>? = psi.run {
        if (psi.containingClass?.qualifiedName?.startsWith("java") == true) return null
        // 公开的、无参数、有返回值，get或is开头
        if (!hasModifierProperty("public") && !hasModifierProperty("static")) return null
        if (parameterList.parametersCount > 0) return null
        if (BooleanUtils.isFalse(name.startsWith("get"))) return null
        returnTypeElement?.let { service.getBaseClass(it) }?.let {
            val fieldName = name.replaceFirst("get", "").let { it.first().lowercase() + it.substring(1) }
            return fieldName to it
        } ?: return null
    }

    /**
     * 解析setter方法
     */
    override fun parseBaseClassAtSetter(
        psi: PsiMethod, call: PsiCallExpression?, root: ClassType
    ): Boolean? = psi.getSetterFieldName()?.let { fieldName ->
        track.getLeadValue(fieldName, call) { psi.parameterList.parameters.first().type.getBaseClass() }
            ?.also { root.putField(fieldName, it) }
    } != null


    /**
     * 解析map.put
     */
    override fun parseBaseClassAtMapPut(
        psi: PsiMethod, root: ClassType, call: PsiCallExpression
    ): Boolean? = psi.run {
        if (name != "put" || parameterList.parametersCount != 2) return false
        call.argumentList?.expressions?.apply {
            val fieldName = service.getConstantValue(first()).toString()
            val type =
                track.getLeadValue(service.getConstantValue(last()).toString(), call) { service.parseBaseClass(last()) }
            root.putField(fieldName, type)
        }
        true
    }

    /**
     * 解析常量文本
     */
    override fun parseLiteralBaseClass(
        psi: PsiLiteralExpression
    ): StaticValueClass? {
        var value = psi.value ?: return null// service.getConstantValue(psi) ?: return null
        var fieldType = getFieldType(value)
        printAllTrack("${value}不是基本类型!!", fieldType.baseClass == null, currentThread = currentThread())
        return StaticValueClass(value, getFieldType(value))
    }

    /**
     * 解析赋值 this.data = data
     */
    override fun parseAssignmentBaseClass(
        psi: PsiAssignmentExpression, root: ClassType
    ): ClassType? = psi.run {
        this.firstChild.lastChild.text?.let {
            root.putField(
                it, track.getLeadValue(it, psi) { service.parseBaseClass(lastChild) })
        }
        root
    }

    /**
     * 解析引用类
     */
    override fun parseJavaCodeRefBaseClass(
        psi: PsiJavaCodeReferenceElement
    ): BaseClass? = psi.run {
        when (val resolve = this.resolve()) {
            is PsiVariable -> service.parseVariableBaseClass(resolve)
            is PsiMethod -> service.parseMethodBaseClass(resolve, psi.parentOfType<PsiCallExpression>())
            is PsiClass -> service.getBaseClass(psi = resolve)
            else -> {
                printAllTrack("未识别的引用类型：$resolve", currentThread = Thread.currentThread())
                null
            }
        }
    }

    override fun parseConditionalBaseClass(
        psi: PsiConditionalExpression
    ): BaseClass? = psi.run {
        return thenExpression?.let { service.parseBaseClass(it, it) }?.takeIf { it.checkValid() }
            ?: elseExpression?.let { service.parseBaseClass(it, it)?.takeIf { it.checkValid() } }
    }

    /**
     * 解析常量值
     */
    override fun getConstantValue(psi: PsiElement): Any? =
        JavaPsiFacade.getInstance(project).constantEvaluationHelper.computeConstantExpression(psi)?.toString()
            ?: psi.text

    override fun parseVariableBaseClass(psi: PsiVariable): BaseClass? = track.getLeadValue(psi.name!!) {
        val root = service.getBaseClass(psi.type) ?: return@getLeadValue null
        root as? ClassType ?: return@getLeadValue root
        // 解析赋值
        ReferencesSearch.search(psi).forEach {
            val parent =
                it.element.parentOfType<PsiExpressionStatement>()?.firstChild?.takeIf { it.text.startsWith(psi.name!!) }
                    ?: return@forEach

            when (parent) {
                // result.data = new ArrayList<Integer>();
                is PsiAssignmentExpression -> service.parseAssignmentBaseClass(parent, root)
                // result.setData(new ArrayList<ResultData<Integer>>());
                is PsiMethodCallExpression -> parent.resolveMethod()?.let { method ->
                    service.parseParamsBaseClass(method, root, parent)
                }

                else -> null
            }
        }
        root
    }
}

internal val targetClass = PsiParseJavaService::class.java
internal val targetClassImpl = PsiParseClassServiceImpl::class.java
val parseLog = Logger.getInstance(targetClassImpl)

@JvmName("printLog")
inline fun printDebugLog(isFlag: Boolean = true, func: () -> String) {
    if (isFlag && isDebugOrTests) println("\n${func()}\n")
}

@JvmName("printAllTrack")
fun printAllTrack(errMsg: String? = null, isPrint: Boolean = true, currentThread: Thread = currentThread()) {
    if (!isPrint || !isDebugOrTests) return
    if (errMsg != null) printDebugLog { errMsg }
    // RuntimeException(errMsg).printStackTrace()
    currentThread.stackTrace.filter { it.isParseStack() }.joinToString(" ; ").toString().also { printDebugLog { it } }
}

@JvmName("printDebugLog")
fun StackTraceElement.isParseStack(): Boolean =
    this.className.takeIf { it.endsWith(targetClassImpl.simpleName) || it.endsWith(targetClass.simpleName) } != null

@JvmName("getSetterFieldName")
fun PsiMethod.getSetterFieldName(): String? {
    if (!this.hasModifierProperty("public") || !name.startsWith("set")) return null

    if (returnTypeElement?.text == VOID || parameterList.parametersCount != 1) return null

    return name.replaceFirst("set", "").let { it.first().lowercase() + it.substring(1) }
}