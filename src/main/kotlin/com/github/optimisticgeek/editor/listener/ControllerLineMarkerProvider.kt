package com.github.optimisticgeek.editor.listener

import com.github.optimisticgeek.analyze.model.AnalyzeMethod
import com.github.optimisticgeek.spring.ext.analyze
import com.github.optimisticgeek.spring.ext.createControllerModel
import com.github.optimisticgeek.spring.model.MethodModel
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.impl.IconLineMarkerProvider
import com.intellij.ide.DataManager
import com.intellij.ide.actions.SmartPopupActionGroup
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.scale.JBUIScale
import com.intellij.util.IconUtil
import javax.swing.JLabel

/**
 * ControllerLineMarkerListener

 * @author OptimisticGeek
 * @date 2023/12/31
 */
class ControllerLineMarkerProvider : IconLineMarkerProvider() {

    override fun collectSlowLineMarkers(
        elements: MutableList<out PsiElement>, result: MutableCollection<in LineMarkerInfo<*>>
    ) {
        elements.mapNotNull { if (it is PsiClass) it else null }.mapNotNull { it.createControllerModel() }
            .filter { it.methodMap?.isNotEmpty() == true }
            .forEach { it.methodMap?.values?.forEach { result.add(it.create()) } }
        super.collectSlowLineMarkers(elements, result)
    }

    private fun MethodModel.create(): LineMarkerInfo<PsiElement> {
        return analyze().create(psiMethod, psiMethodAnnotation)
    }

    private fun AnalyzeMethod.create(
        psiMethod: PsiMethod, psiAnnotation: PsiAnnotation
    ): LineMarkerInfo<PsiElement> {
        val title = this.remark ?: this.name ?: ""
        val icon = IconUtil.textToIcon(this.httpMethod.name, JLabel(), JBUIScale.scale(10.0f))
        return LineMarkerInfo<PsiElement>(
            /* element = */ psiMethod.nameIdentifier!!,
            /* range = */ psiAnnotation.textRange,
            /* icon = */ icon,
            /* tooltipProvider = */ { title },
            /* navHandler = */ { e, ele ->
                run {
                    val smartPopupActionGroup = SmartPopupActionGroup()
                    val editor = FileEditorManager.getInstance(ele.project).selectedTextEditor
                    val dataContext = DataManager.getInstance().getDataContext(editor!!.contentComponent)
                    val actionGroupPopup = JBPopupFactory.getInstance().createActionGroupPopup(
                        null, smartPopupActionGroup, dataContext, JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, true
                    )
                    actionGroupPopup.show(RelativePoint(e))
                }
            },
            /* alignment = */ GutterIconRenderer.Alignment.LEFT,
            /* accessibleNameProvider = */ { title }
        )
    }
}

