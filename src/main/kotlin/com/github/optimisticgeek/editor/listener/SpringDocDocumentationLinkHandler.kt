// Copyright 2023-2024 79127. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimisticgeek.editor.listener

import com.github.optimisticgeek.analyze.model.AnalyzeMethod
import com.github.optimisticgeek.analyze.model.AnalyzeModel
import com.intellij.configurationStore.NOTIFICATION_GROUP_ID
import com.intellij.lang.documentation.DocumentationLinkHandler
import com.intellij.lang.documentation.DocumentationTarget
import com.intellij.lang.documentation.LinkResolveResult
import com.intellij.lang.documentation.psi.PsiElementDocumentationTarget
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.util.concurrency.AppExecutorUtil
import java.awt.datatransfer.StringSelection
import java.util.concurrent.TimeUnit

/**
 * SpringApiDocumentProvider

 * @author OptimisticGeek
 * @date 2024/1/1
 */
@Suppress("UnstableApiUsage")
class SpringDocDocumentationLinkHandler : DocumentationLinkHandler {

    override fun resolveLink(target: DocumentationTarget, url: String): LinkResolveResult? {
        if (target !is PsiElementDocumentationTarget) return super.resolveLink(target, url)
        if (!url.contains("###")) return super.resolveLink(target, url)

        val split = url.split("###")
        if (split.size <= 2) return super.resolveLink(target, url)

        val model = target.targetElement.getUserData(documentationKey) ?: return super.resolveLink(target, url)
        val project = target.targetElement.project
        split[1].let { keyword ->
            if (model is AnalyzeModel && keyword == modelKey) model.command(project, split[2])
            if (model is AnalyzeMethod) {
                when (keyword) {
                    pathParamsKey -> model.pathParams?.command(project, split[2])
                    queryParamsKey -> model.queryParams?.command(project, split[2])
                    requestBodyKey -> model.requestBody?.command(project, split[2])
                    responseKey -> model.response?.command(project, split[2])
                    linkKey -> project.copyString(model.urls.joinToString("\n"))
                    else -> return null
                }
            }
            return null
        }
    }

    private fun AnalyzeModel.command(project: Project, command: String) {
        when (command) {
            commandCopyHtml -> project.copyString(this.toHtmlDocument())
            commandCopyJson -> project.copyString(this.toJson())
            else -> project.copyString(this.toHtmlDocument())
        }
    }

    private fun AnalyzeMethod.command(project: Project, command: String) {
        when (command) {
            commandCopyHtml -> project.copyString(this.toHtmlDocument())
            commandCopyJson -> project.copyString(this.toHtmlDocument())
            else -> project.copyString(this.toHtmlDocument())
        }
    }
}

private fun Project.copyString(str: String?) {
    val stringSelection = StringSelection(str)
    CopyPasteManager.getInstance().setContents(stringSelection)

    Notification(
        NOTIFICATION_GROUP_ID, "提醒", "已复制", NotificationType.INFORMATION
    ).let { notification: Notification ->
        Notifications.Bus.notify(notification, this)
        AppExecutorUtil.getAppScheduledExecutorService().schedule({ notification.expire() }, 2, TimeUnit.SECONDS)
    }
}


internal const val commandCopyHtml = "Copy"
internal const val commandCopyJson = "Copy Json"
