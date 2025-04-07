package com.github.optimistic.spring.parse

import com.github.optimistic.spring.model.ClassType
import com.github.optimistic.spring.model.PsiParseThreadTrack
import com.github.optimistic.spring.model.type.BaseClass
import com.github.optimistic.spring.model.type.ObjClass
import com.github.optimistic.spring.model.type.StaticValueClass
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.*
import java.lang.reflect.Proxy

@Service(Service.Level.PROJECT)
class PsiParseJavaService(project: Project) : PsiParseClassService {
    val parse by lazy { PsiParseClassServiceImpl(project) }

    // 创建代理实例
    val instance by lazy {
        Proxy.newProxyInstance(
            this.javaClass.classLoader, this.javaClass.interfaces, PsiParseClassHandler(parse)
        ) as PsiParseClassService
    }

    override fun getBaseClass(psi: PsiTypeElement?): BaseClass? = instance.getBaseClass(psi?.type)

    override fun getBaseClass(psi: PsiType?): BaseClass? = instance.getBaseClass(psi)

    override fun getBaseClass(qName: String?, psi: PsiClass?): BaseClass? = instance.getBaseClass(qName, psi)

    override fun parseBaseClass(psi: PsiElement, from: PsiElement?): BaseClass? = instance.parseBaseClass(psi, from)

    override fun refreshFields(psi: PsiClass, obj: ObjClass, isForce: Boolean?): ClassType =
        instance.refreshFields(psi, obj, isForce)

    override fun parseMethodBaseClass(psi: PsiMethod, call: PsiCallExpression?): BaseClass? =
        instance.parseMethodBaseClass(psi, call)

    override fun parseReturnBaseClass(psi: PsiMethod, call: PsiCallExpression?): BaseClass? =
        instance.parseReturnBaseClass(psi, call)

    override fun parseReturnBaseClass(psi: PsiReturnStatement): BaseClass? = instance.parseReturnBaseClass(psi)

    override fun parseParamsBaseClass(psi: PsiNewExpression, call: PsiCallExpression?): BaseClass? =
        instance.parseParamsBaseClass(psi, call)

    override fun parseParamsBaseClass(psi: PsiMethod, root: ClassType, call: PsiCallExpression?): ClassType? =
        instance.parseParamsBaseClass(psi, root, call)

    override fun parseMethodCallBaseClass(psi: PsiMethodCallExpression, root: ClassType?): BaseClass? =
        instance.parseMethodCallBaseClass(psi, root)

    override fun putLead(psi: PsiMethod, call: PsiCallExpression?, root: ClassType) = instance.putLead(psi, call, root)

    override fun parseBaseClassAtGetter(psi: PsiMethod): Pair<String, BaseClass>? = instance.parseBaseClassAtGetter(psi)

    override fun parseBaseClassAtSetter(psi: PsiMethod, call: PsiCallExpression?, root: ClassType): Boolean? =
        instance.parseBaseClassAtSetter(psi, call, root) == true

    override fun parseBaseClassAtMapPut(psi: PsiMethod, root: ClassType, call: PsiCallExpression): Boolean? =
        instance.parseBaseClassAtMapPut(psi, root, call) == true

    override fun parseLiteralBaseClass(psi: PsiLiteralExpression): StaticValueClass? =
        instance.parseLiteralBaseClass(psi)

    override fun parseAssignmentBaseClass(psi: PsiAssignmentExpression, root: ClassType): ClassType? =
        instance.parseAssignmentBaseClass(psi, root)

    override fun parseJavaCodeRefBaseClass(psi: PsiJavaCodeReferenceElement): BaseClass? =
        instance.parseJavaCodeRefBaseClass(psi)

    override fun parseConditionalBaseClass(psi: PsiConditionalExpression): BaseClass? =
        instance.parseConditionalBaseClass(psi)

    override fun getConstantValue(psi: PsiElement): Any? = instance.getConstantValue(psi)

    override fun parseVariableBaseClass(psi: PsiVariable): BaseClass? = instance.parseVariableBaseClass(psi)
}


// 创建一个动态代理处理器

@JvmName("psiParseJavaService")
fun Project.parseService(): PsiParseClassService = service<PsiParseJavaService>().instance

@JvmName("psiParseJavaService")
fun PsiElement.parseService(): PsiParseClassService = project.parseService()

@JvmName("psiParseJavaService")
fun PsiType.parseService(): PsiParseClassService? = resolveScope?.project?.parseService()

@JvmField
val ObjClassKey = Key.create<ObjClass>("BASE_CLASS")

@JvmField
val ParseTrackKey = Key.create<PsiParseThreadTrack>("PARSE_TRACK")