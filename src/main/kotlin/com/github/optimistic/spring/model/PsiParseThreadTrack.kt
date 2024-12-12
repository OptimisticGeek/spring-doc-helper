package com.github.optimistic.spring.model

import com.github.optimistic.analyze.model.analyze
import com.github.optimistic.editor.listener.toJson
import com.github.optimistic.spring.ext.className
import com.github.optimistic.spring.model.type.BaseClass
import com.github.optimistic.spring.model.type.checkValid
import com.github.optimistic.spring.parse.*
import com.intellij.internal.DebugAttachDetector
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.*
import com.intellij.psi.util.parentOfType
import com.intellij.remoteDev.tests.isDistributedTestMode
import org.apache.commons.lang3.StringUtils

data class PsiParseThreadTrack(
    val source: SmartPsiElementPointer<PsiElement>,
    var parent: PsiParseThreadTrack? = null,
    val depth: Int = parent?.depth?.plus(1) ?: 0,
    val children: MutableList<PsiParseThreadTrack> = ArrayList(),
) {
    val startTimeMillis: Long = System.currentTimeMillis()
    var endTimeMillis: Long = startTimeMillis
    val currentThread: Thread = Thread.currentThread()
    val threadTrack: StackTraceElement =
        currentThread.stackTrace.firstOrNull { it.isParseStack() } ?: currentThread.stackTrace.first()
    val methodName: String get() = threadTrack.methodName ?: StringUtils.EMPTY
    val qName: String get() = threadTrack.className ?: StringUtils.EMPTY
    val className: String get() = qName.className()
    val lineNumber: Int get() = threadTrack.lineNumber

    val leadMap: MutableMap<String, BaseClass> by lazy { parents.lastOrNull()?.leadMap ?: mutableMapOf() }

    constructor(
        psi: PsiElement, parent: PsiParseThreadTrack? = null, depth: Int = parent?.depth?.plus(1) ?: 0
    ) : this(source = SmartPointerManager.createPointer(psi), parent, depth = depth)

    val psiCodeRange: String by lazy {
        source.element?.parentOfType<PsiClass>()
            .let { "psi: ${source.element.toString()} - ${it?.qualifiedName.toString()}${source.element?.textRange ?: ""}" }
    }

    val parents: ArrayList<PsiParseThreadTrack> by lazy {
        ArrayList<PsiParseThreadTrack>().apply { parent?.also { add(it) }?.parents?.also { addAll(it) } }
    }

    val shortText: String by lazy {
        buildString {
            if (parent?.psiCodeRange != psiCodeRange) {
                appendLine("\n --- $psiCodeRange\n")
                source.element?.let { if (it.textLength > 50) "${it.text.substring(0, 50)}..." else it.text }
                    ?.let { appendLine("\n$it\n") }
            }
            appendLine(this)
            appendLine("线索：$leadMap")
            appendLine("==> ${this@PsiParseThreadTrack} - $startTimeMillis")
        }
    }

    init {
        parent?.let { leadMap.putAll(it.leadMap) }
    }

    /**
     * 创建成功时返回对象，失败返回null (深度问题、循环调用检测)
     */
    fun create(psi: PsiElement?, isSelf: Boolean = false): PsiParseThreadTrack {
        if (isSelf || psi == null) return this
        return PsiParseThreadTrack(psi, this).also { this.endTimeMillis = System.currentTimeMillis() }
            .also { children.add(it) }

    }

    /**
     * 检测深度
     */
    fun checkDepth(): Boolean =
        takeIf { depth > 300 }?.also { printAllTrack("超过最大解析深度:$depth > 300", currentThread = currentThread) }
            ?.let { true } == true

    /**
     * 循环调用检测
     */
    fun checkLoopCall(): Boolean {
        return false
        return parents.firstOrNull { it.source.element.toString() == source.element.toString() && it.methodName == methodName }
            ?.also {
                printAllTrack(
                    "栈溢出:${it.source.element.toString()} - ${it.methodName}", currentThread = currentThread
                )
            } != null
    }

    override fun toString(): String = "$depth - $qName:$lineNumber - $methodName"

    override fun equals(other: Any?): Boolean =
        other?.let { other as? PsiParseThreadTrack }?.let { hashCode() == it.hashCode() } == true

    override fun hashCode(): Int = source.hashCode() * 31 + methodName.hashCode()

    fun putLeadToMap(key: String?, value: BaseClass?) {
        key ?: return
        value ?: return
        value.takeIf { leadMap.containsKey(key) && !value.checkValid() }
            ?.also { printDebugLog { "【×】添加线索：$key = $value" } }?.also { return }
        leadMap[key] =
            value.apply { printDebugLog(value is ClassType && value.checkValid()) { "[√]添加线索：$key = $value" } }
    }

    fun getLead(paramName: String?, call: PsiExpression? = null, defaultFunc: () -> BaseClass? = { null }): BaseClass? {
        // 优先使用默认方法的值
        val default = defaultFunc()?.also { if (it.checkValid()) return it }
        // 字段名匹配
        getLeadBaseClass(paramName)?.also { return it }
        call?.also { getLeadBaseClass(call)?.also { return it } }

        return when (val ele = source.element) {
            is PsiMethod -> ele.getSetterFieldName()?.takeIf { it == paramName }
                ?.let { getLeadBaseClass("${ele.name}#0") }

            else -> null
        } ?: default
    }

    /**
     * 标记已完成
     */
    @JvmName("finished")
    fun <T : BaseClass> finished(root: T? = null, isFinished: Boolean = true, after: () -> Unit = {}): T? {
        if (!isFinished) return root
        endTimeMillis = System.currentTimeMillis()
        printAllTrack(
            "解析完成：$this，总耗时:${endTimeMillis - startTimeMillis}\n${root?.analyze()?.toJson()}",
            currentThread = currentThread
        )

        print("解析完成：$this，总耗时:${endTimeMillis - startTimeMillis}\n${root?.analyze()?.toJson()}")
        if (isDebugOrTests && !root.checkValid()) {
            printDebugLog { "解析失败或不完整: $root" }
            printDebugLog { shortText }
        }

        after()
        parseThreadTrack.clear()
        return root
    }

    /**
     * 尝试从线索中更新字段
     */
    fun <T : BaseClass> refreshFromLeads(root: T?, key: String? = null): Boolean {
        if (leadMap.isEmpty()) return false
        if (root == null || root !is ClassType) return false
        if (root.checkValid()) return true
        // 目前仅支持字段线索搜寻
        root.targetFields.map { it.name }.forEach { fieldName ->
            val type = getLeadValue(fieldName) { key?.let { leadMap[fieldName] } } ?: return false

            type.takeIf { root.putField(fieldName, it) }
                ?.also { printDebugLog { "通过线索补充了字段：$root - [$fieldName = $it]" } } != null
        }
        return root.checkValid()
    }

    private fun getLeadBaseClass(key: String?): BaseClass? =
        leadMap[key]?.also { printDebugLog(it is ClassType) { "命中线索：$key -> $it" } }

    private fun getLeadBaseClass(call: PsiExpression?): BaseClass? {
        val key = if(call is PsiCall) call.argumentList?.expressions?.firstOrNull() ?: call.text else call?.text ?: return null
        return leadMap[key]?.also { printDebugLog(it is ClassType) { "命中线索：$key -> $it" } }
    }

}

@JvmName("getLead")
fun PsiParseThreadTrack?.getLeadValue(
    paramName: String,
    call: PsiExpression? = null,
    defaultFunc: () -> BaseClass? = { null }
): BaseClass? =
    this?.getLead(paramName, call, defaultFunc)

val isDebugOrTests = DebugAttachDetector.isDebugEnabled() || ApplicationManager.getApplication()
    .let { it.isUnitTestMode || it.isDistributedTestMode }
