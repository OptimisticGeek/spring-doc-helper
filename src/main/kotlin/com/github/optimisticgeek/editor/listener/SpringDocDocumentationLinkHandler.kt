// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimisticgeek.editor.listener

import com.intellij.openapi.editor.event.EditorMouseListener

/**
 * SpringApiDocumentProvider

 * @author OptimisticGeek
 * @date 2024/1/1
 */
class SpringDocDocumentationLinkHandler : EditorMouseListener {

}
    /*
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
                    }*/
/*
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
*/





