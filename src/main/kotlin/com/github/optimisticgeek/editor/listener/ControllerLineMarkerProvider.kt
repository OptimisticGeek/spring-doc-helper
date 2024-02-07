// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimisticgeek.editor.listener

import com.github.optimisticgeek.analyze.model.AnalyzeMethod
import com.github.optimisticgeek.spring.ext.analyze
import com.github.optimisticgeek.spring.ext.createControllerModel
import com.github.optimisticgeek.spring.model.MethodModel
import com.github.optimisticgeek.spring.service.ScannerBundle
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.impl.JavaLineMarkerProvider
import com.intellij.configurationStore.NOTIFICATION_GROUP_ID
import com.intellij.ide.DataManager
import com.intellij.ide.actions.SmartPopupActionGroup
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.scale.JBUIScale
import com.intellij.util.IconUtil
import com.intellij.util.concurrency.AppExecutorUtil
import java.awt.datatransfer.StringSelection
import java.util.concurrent.TimeUnit
import javax.swing.Icon
import javax.swing.JLabel

/**
 * ControllerLineMarkerListener

 * @author OptimisticGeek
 * @date 2023/12/31
 */
class ControllerLineMarkerProvider : JavaLineMarkerProvider() {

    override fun collectSlowLineMarkers(
        elements: MutableList<out PsiElement>, result: MutableCollection<in LineMarkerInfo<*>>
    ) {
        elements.mapNotNull { if (it is PsiClass) it else null }.mapNotNull { it.createControllerModel() }
            .filter { it.methodMap?.isNotEmpty() == true }
            .forEach {
                it.methodMap?.values?.forEach {
                    result.add(
                        it.analyze()
                            .createLineMarkerInfo(it.psiMethod.nameIdentifier, it.textRange(), it.title(), it.icon())
                    )
                }
            }
        super.collectSlowLineMarkers(elements, result)
    }
}

private fun AnalyzeMethod.createLineMarkerInfo(
    identifier: PsiIdentifier?, range: TextRange, title: String, icon: Icon
): LineMarkerInfo<PsiIdentifier> {
    return LineMarkerInfo<PsiIdentifier>(/* element = */ identifier!!, /* range = */ range, /* icon = */ icon,
        /* tooltipProvider = */ { title },
        /* navHandler = */
        { e, ele ->
            run {
                val smartPopupActionGroup = SmartPopupActionGroup()
                smartPopupActionGroup.add(object :
                    AnAction(ScannerBundle.message("copy.title", ScannerBundle.message("document.url"))) {
                    override fun actionPerformed(e: AnActionEvent) {
                        urls.joinToString(",").let { e.project?.copyString(it) }
                    }
                })
                pathParams?.let {
                    smartPopupActionGroup.add(object :
                        AnAction(ScannerBundle.message("copy.title", ScannerBundle.message("document.pathParams"))) {
                        override fun actionPerformed(e: AnActionEvent) {
                            it.toJson().let { e.project?.copyString(it) }
                        }
                    })
                }

                queryParams?.let {
                    smartPopupActionGroup.add(object :
                        AnAction(ScannerBundle.message("copy.title", ScannerBundle.message("document.queryParams"))) {
                        override fun actionPerformed(e: AnActionEvent) {
                            it.toJson().let { e.project?.copyString(it) }
                        }
                    })
                }

                requestBody?.let {
                    smartPopupActionGroup.add(object :
                        AnAction(ScannerBundle.message("copy.title", ScannerBundle.message("document.requestBody"))) {
                        override fun actionPerformed(e: AnActionEvent) {
                            it.toJson().let { e.project?.copyString(it) }
                        }
                    })
                }

                response?.let {
                    smartPopupActionGroup.add(object :
                        AnAction(ScannerBundle.message("copy.title", ScannerBundle.message("document.response"))) {
                        override fun actionPerformed(e: AnActionEvent) {
                            it.toJson().let { e.project?.copyString(it) }
                        }
                    })
                }

                val editor = FileEditorManager.getInstance(ele.project).selectedTextEditor
                val dataContext = DataManager.getInstance().getDataContext(editor!!.contentComponent)
                val actionGroupPopup = JBPopupFactory.getInstance().createActionGroupPopup(
                    null, smartPopupActionGroup, dataContext, JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, true
                )
                actionGroupPopup.show(RelativePoint(e))
            }
        },
        /* alignment = */
        GutterIconRenderer.Alignment.LEFT, /* accessibleNameProvider = */
        { title }
    )
}

private fun MethodModel.textRange(): TextRange {
    return psiMethodAnnotation.textRange
}

private fun MethodModel.icon(): Icon {
    return IconUtil.textToIcon(this.requestMethod.name, JLabel(), JBUIScale.scale(10.0f))
}

private fun MethodModel.title(): String {
    return this.remark ?: this.name ?: ""
}

fun Project.copyString(str: String) {
    CopyPasteManager.getInstance().setContents(StringSelection(str))

    Notification(
        NOTIFICATION_GROUP_ID,
        ScannerBundle.message("copy.title", ""),
        ScannerBundle.message("copy.success", str.substring(0, if (str.length > 50) 50 else str.length)),
        NotificationType.INFORMATION
    ).let { notification: Notification ->
        Notifications.Bus.notify(notification, this)
        AppExecutorUtil.getAppScheduledExecutorService().schedule({ notification.expire() }, 2, TimeUnit.SECONDS)
    }
}
