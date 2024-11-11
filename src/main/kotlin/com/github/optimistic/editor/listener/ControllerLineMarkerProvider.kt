// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimistic.editor.listener

import com.github.optimistic.analyze.model.AnalyzeHttpMethod
import com.github.optimistic.analyze.model.toCurlStr
import com.github.optimistic.editor.httpClient.createHttpTestFile
import com.github.optimistic.editor.httpClient.hasHttpTestMethod
import com.github.optimistic.spring.ext.getHttpRequestAnnotation
import com.github.optimistic.spring.model.HttpMethodModel
import com.github.optimistic.spring.service.ScannerBundle
import com.github.optimistic.spring.service.getHttpMethodMap
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
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
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
        elements: MutableList<out PsiElement>, result: MutableCollection<in LineMarkerInfo<*>>
    ) {
        elements.filterIsInstance<PsiClass>().filter { !DumbService.isDumb(it.project) }.forEach {
            it.getHttpMethodMap()?.values?.asSequence()?.map {
                it.analyze().createLineMarkerInfo(it.psiMethod.nameIdentifier, it.textRange(), it.title(), it.icon())
            }?.forEach(result::add)
        }
    }
}

@JvmName("analyze")
fun HttpMethodModel.analyze(): AnalyzeHttpMethod = AnalyzeHttpMethod(this)

@JvmName("createLineMarkerInfo")
private fun AnalyzeHttpMethod.createLineMarkerInfo(
    identifier: PsiIdentifier?, range: TextRange, title: String, icon: Icon
): LineMarkerInfo<PsiIdentifier> {
    return LineMarkerInfo<PsiIdentifier>(/* element = */ identifier!!, /* range = */
        range, /* icon = */
        icon,/* tooltipProvider = */
        { title },/* navHandler = */
        { e, _ ->
            SmartPopupActionGroup.createPopupGroup { title }.apply {
                if (hasHttpTestMethod()) {
                    add(ScannerBundle.message("action.api.show")) { createHttpTestFile() }
                    add(ScannerBundle.message("action.api.flushed")) { createHttpTestFile(flushed = true) }
                } else {
                    add(ScannerBundle.message("action.api.create")) { createHttpTestFile() }
                }
                add(ScannerBundle.message("action.copy.curl")) { toCurlStr().let { project.copyString(it) } }
                add(ScannerBundle.message("action.copy.url")) {
                    getUrl(false, hasRootUrl = true).let { project.copyString(it) }
                }
                add(createJsonCopyActionGroup())
                add(createCopyJsonSchemaActionGroup())
            }.also { popupActionGroup(it, e) }
        },/* alignment = */
        GutterIconRenderer.Alignment.LEFT, /* accessibleNameProvider = */
        { title })
}

@JvmName("popupActionGroup")
private fun AnalyzeHttpMethod.popupActionGroup(group: DefaultActionGroup, e: MouseEvent) {
    FileEditorManager.getInstance(project).selectedTextEditor!!.let {
        DataManager.getInstance().getDataContext(it.contentComponent)
    }.let {
        JBPopupFactory.getInstance().createActionGroupPopup(
            null, group, it, JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, true
        )
    }.apply { isShowSubmenuOnHover = true }.apply { setMinimumSize(Dimension(10, 0)) }.show(RelativePoint(e))
}

/*
@JvmName("createHttpClientActionGroup")
private fun AnalyzeMethod.createHttpClientActionGroup(isCreated: Boolean): DefaultActionGroup {
    return SmartPopupActionGroup.createPopupGroup { ScannerBundle.message("action.api.test") }.apply {
        if (!isCreated)
            add(ScannerBundle.message("action.api.create")) { createHttpTestFile() }
        else {
            add(ScannerBundle.message("action.api.show")) { createHttpTestFile() }
            add(ScannerBundle.message("action.api.flushed")) { createHttpTestFile(reCreate = true) }
        }
    }
}
*/

@JvmName("textRange")
private fun HttpMethodModel.textRange(): TextRange = psiMethod.getHttpRequestAnnotation()!!.textRange

@JvmName("icon")
private fun HttpMethodModel.icon(): Icon = httpMethod.icon

@JvmName("title")
private fun HttpMethodModel.title(): String = this.remark

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


@JvmName("createCopyJsonActionGroup")
fun AnalyzeHttpMethod.createJsonCopyActionGroup(): DefaultActionGroup {
    return SmartPopupActionGroup.createPopupGroup { ScannerBundle.message("action.copyJson.title") }.apply {
        pathParams?.let {
            add(ScannerBundle.message("document.pathParams")) { it.toJson().let { project.copyString(it) } }
        }

        queryParams?.let {
            add(ScannerBundle.message("document.queryParams")) { it.toJson().let { project.copyString(it) } }
        }

        requestBody?.let {
            add(ScannerBundle.message("document.requestBody")) { it.toJson().let { project.copyString(it) } }
        }

        response?.let {
            add(ScannerBundle.message("document.response")) { it.toJson().let { project.copyString(it) } }
        }
    }
}

@JvmName("createCopyJsonSchemaActionGroup")
fun AnalyzeHttpMethod.createCopyJsonSchemaActionGroup(): DefaultActionGroup {
    return SmartPopupActionGroup.createPopupGroup { ScannerBundle.message("action.copyJsonSchema.title") }.apply {
        pathParams?.let {
            add(ScannerBundle.message("document.pathParams")) { it.toJsonSchema().let { project.copyString(it) } }
        }

        queryParams?.let {
            add(ScannerBundle.message("document.queryParams")) { it.toJsonSchema().let { project.copyString(it) } }
        }

        requestBody?.let {
            add(ScannerBundle.message("document.requestBody")) { it.toJsonSchema().let { project.copyString(it) } }
        }

        response?.let {
            add(ScannerBundle.message("document.response")) { it.toJsonSchema().let { project.copyString(it) } }
        }
    }
}

@JvmName("addAnActionGroup")
fun DefaultActionGroup.add(title: String, function: () -> Unit) {
    add(object : AnAction(title) {
        override fun actionPerformed(e: AnActionEvent) {
            function.invoke()
        }
    })
}

