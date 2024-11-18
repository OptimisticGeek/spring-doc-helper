// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimistic.editor.listener

import com.github.optimistic.analyze.model.AnalyzeHttpMethod
import com.github.optimistic.spring.constant.HttpMethodType
import com.github.optimistic.spring.ext.getHttpMethodType
import com.github.optimistic.spring.ext.getHttpRequestAnnotation
import com.github.optimistic.spring.model.HttpMethodModel
import com.github.optimistic.spring.service.ScannerBundle
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.impl.JavaLineMarkerProvider
import com.intellij.configurationStore.NOTIFICATION_GROUP_ID
import com.intellij.ide.DataManager
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.PsiMethod
import com.intellij.ui.awt.RelativePoint
import com.intellij.util.concurrency.AppExecutorUtil
import java.awt.Dimension
import java.awt.datatransfer.StringSelection
import java.awt.event.MouseEvent
import java.util.concurrent.TimeUnit
import javax.swing.Icon

/**
 * ControllerLineMarkerListener

 * @author OptimisticGeek
 * @date 2023/12/31
 */
class ControllerLineMarkerProvider : JavaLineMarkerProvider() {

    override fun collectSlowLineMarkers(
        elements: List<PsiElement?>,
        result: MutableCollection<in LineMarkerInfo<*>>
    ) {
        elements.filterIsInstance<PsiMethod>()
            .forEach { element ->
                element.getHttpRequestAnnotation()
                    ?.let {
                        element.createLineMarkerInfo(it.textRange, "", it.getHttpMethodType(HttpMethodType.ALL)!!.icon)
                    }?.let { result.add(it) }
            }
    }

    private fun PsiMethod.createLineMarkerInfo(
        range: TextRange, title: String, icon: Icon
    ): LineMarkerInfo<PsiIdentifier> {
        return LineMarkerInfo<PsiIdentifier>(
            nameIdentifier!!,
            range,
            icon,
            { title },
            { e, psi -> popupActionGroup(e) },
            GutterIconRenderer.Alignment.LEFT,
            { title })
    }

    private fun PsiMethod.popupActionGroup(e: MouseEvent) {
        // 获取当前编辑窗口的DataContext
        val editor = FileEditorManager.getInstance(project).selectedTextEditor!!
        val context = editor.let { DataManager.getInstance().getDataContext(it.contentComponent) }
        editor.caretModel.moveToOffset(this.nameIdentifier!!.textOffset)
        val action = ActionManager.getInstance().getAction("SpringDocHelper.Copy")
        action as DefaultActionGroup

        JBPopupFactory.getInstance().createActionGroupPopup(
            null, action, context, JBPopupFactory.ActionSelectionAid.NUMBERING, true
        ).apply { isShowSubmenuOnHover = true }.apply { setMinimumSize(Dimension(10, 0)) }.show(RelativePoint(e))
    }
}

@JvmName("analyze")
fun HttpMethodModel.analyze(): AnalyzeHttpMethod = AnalyzeHttpMethod(this)

@JvmName("copyString")
fun Project.copyString(str: String) {
    CopyPasteManager.getInstance().setContents(StringSelection(str))

    Notification(
        NOTIFICATION_GROUP_ID,
        ScannerBundle.message("action.copy", ""),
        ScannerBundle.message("action.copy.success", str),
        NotificationType.INFORMATION
    ).let { notification: Notification ->
        Notifications.Bus.notify(notification, this)
        AppExecutorUtil.getAppScheduledExecutorService().schedule({ notification.expire() }, 3, TimeUnit.SECONDS)
    }
}




