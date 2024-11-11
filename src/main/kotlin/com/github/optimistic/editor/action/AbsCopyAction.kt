package com.github.optimistic.editor.action

import com.github.optimistic.analyze.model.AnalyzeHttpMethod
import com.github.optimistic.analyze.model.AnalyzeModel
import com.github.optimistic.editor.listener.analyze
import com.github.optimistic.editor.listener.copyString
import com.github.optimistic.spring.service.ScannerBundle
import com.github.optimistic.spring.service.getHttpMethod
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.parentOfType

abstract class AbsCopyAction(key: String) : AnAction(ScannerBundle.message(key)) {
    protected var method: AnalyzeHttpMethod? = null

    open fun getModel(): AnalyzeModel? {
        return null
    }

    open fun isVisible(): Boolean {
        return getModel()?.let { true } ?: false
    }

    override fun update(e: AnActionEvent) {
        method = e.getAnalyzeHttpMethod()
        e.presentation.isEnabledAndVisible = isVisible()
    }

    fun AnActionEvent.copyString(str: String?) {
        str?.let { project?.copyString(it) }
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}

abstract class AbsCopyActionGroup(key: String) : DefaultActionGroup() {

    init {
        this.templatePresentation.setText { ScannerBundle.message(key) }
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.getAnalyzeHttpMethod() != null
        e.presentation.isDisableGroupIfEmpty = true
    }
}

class ControllerActionGroup : AbsCopyActionGroup("action.copy.title")
class CopyJsonActionGroup : AbsCopyActionGroup("action.copyJson.title")
class CopyJsonSchemaActionGroup : AbsCopyActionGroup("action.copyJsonSchema.title")


@JvmName("anActionEventGetMethod")
fun AnActionEvent.getAnalyzeHttpMethod(): AnalyzeHttpMethod? {
    // 获取当前编辑器
    val editor: Editor = getData(CommonDataKeys.EDITOR) ?: return null

    // 获取当前文件
    val psiFile: PsiFile = getData(CommonDataKeys.PSI_FILE) ?: return null

    // 获取光标位置
    val offset = editor.caretModel.offset

    // 根据光标位置获取PsiElement
    return psiFile.findElementAt(offset)?.parentOfType<PsiMethod>()?.getHttpMethod()?.analyze()
}