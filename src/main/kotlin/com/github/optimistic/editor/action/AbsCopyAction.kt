package com.github.optimistic.editor.action

import com.github.optimistic.analyze.model.AnalyzeHttpMethod
import com.github.optimistic.analyze.model.AnalyzeModel
import com.github.optimistic.analyze.model.toCurlStr
import com.github.optimistic.editor.listener.analyze
import com.github.optimistic.editor.listener.copyString
import com.github.optimistic.editor.listener.getAnalyzeModel
import com.github.optimistic.spring.service.ScannerBundle
import com.github.optimistic.spring.service.getHttpMethod
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.parentOfType
import org.apache.commons.lang3.StringUtils

abstract class AbsCopyAction(key: String?) : AnAction() {
    protected var psi: PsiElement? = null
    protected val method: AnalyzeHttpMethod? by lazy { psi?.getHttpMethod() }

    init {
        key?.let { templatePresentation.setText { ScannerBundle.message(it) } }
    }

    open fun getModel(): AnalyzeModel? = null

    open fun isVisible(): Boolean = getModel() != null

    override fun update(e: AnActionEvent) {
        psi = e.getPsiElement()
        e.presentation.isEnabledAndVisible = isVisible()
    }

    fun AnActionEvent.copyString(str: String?) {
        str?.let { project?.copyString(it) }
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}

/**
 * 鼠标所在Model
 */
abstract class AbsCurrentCopyAction(private val key: String) : AbsCopyAction(key) {
    protected var currentModel: AnalyzeModel? = null
    override fun getModel(): AnalyzeModel? = currentModel

    override fun getTemplateText(): String {
        return ScannerBundle.message(key)
    }

    override fun update(e: AnActionEvent) {
        currentModel = psi?.getAnalyzeModel()
        super.update(e)
    }
}

abstract class AbsCopyActionGroup(key: String) : DefaultActionGroup() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    init {
        templatePresentation.setText { ScannerBundle.message(key) }
    }
}

class ControllerActionGroup : AbsCopyActionGroup("action.copy.title")
class CopyJsonActionGroup : AbsCopyActionGroup("action.copyJson.title")
class CopyJsonSchemaActionGroup : AbsCopyActionGroup("action.copyJsonSchema.title")

@JvmName("anActionEventGetMethod")
private fun PsiElement?.getHttpMethod(): AnalyzeHttpMethod? =
    this?.parentOfType<PsiMethod>()?.getHttpMethod()?.analyze()

// todo 需要兼容左侧小图标的事件
@JvmName("anActionEventGetMethod")
fun AnActionEvent.getPsiElement(): PsiElement? {
    // 获取当前编辑器
    val editor: Editor = getData(CommonDataKeys.EDITOR) ?: return null

    // 获取当前文件
    val psiFile: PsiFile = getData(CommonDataKeys.PSI_FILE) ?: return null

    // 获取光标位置
    val offset = editor.caretModel.offset

    // 根据光标位置获取PsiElement
    return psiFile.findElementAt(offset)
}

/**
 * 复制为Curl
 */
class CopyCurlAction : AbsCopyAction("action.copy.curl") {
    override fun actionPerformed(e: AnActionEvent) = e.copyString(method?.toCurlStr())

    override fun isVisible(): Boolean = method != null
}

/**
 * 复制url
 */
class CopyUrlAction : AbsCopyAction("action.copy.url") {
    override fun actionPerformed(e: AnActionEvent) = e.copyString(method?.getUrl())

    override fun isVisible(): Boolean = StringUtils.isNotBlank(method?.getUrl())
}