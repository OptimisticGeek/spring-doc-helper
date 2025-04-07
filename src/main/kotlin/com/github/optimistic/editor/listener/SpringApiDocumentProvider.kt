// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimistic.editor.listener

import com.github.optimistic.analyze.model.AnalyzeHttpMethod
import com.github.optimistic.analyze.model.AnalyzeModel
import com.github.optimistic.analyze.model.BaseAnalyzeModel
import com.github.optimistic.analyze.model.analyze
import com.github.optimistic.spring.ext.isControllerClass
import com.github.optimistic.spring.index.getHttpMethodModel
import com.github.optimistic.spring.model.ClassType
import com.github.optimistic.spring.parse.PsiParseJavaService
import com.github.optimistic.spring.parse.parseService
import com.intellij.lang.java.JavaDocumentationProvider
import com.intellij.openapi.components.service
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.parentOfType
import org.apache.commons.lang3.StringUtils

/**
 * SpringApiDocumentProvider

 * @author OptimisticGeek
 * @date 2024/1/1
 */
class SpringApiDocumentProvider : JavaDocumentationProvider() {

    /**
     * 鼠标悬浮在@[Get|Post|Put|Delete|request]Mapping注解时，显示完整的api文档
     */
    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        var sourceGenerate = super.generateDoc(element, originalElement)
        originalElement?.let { buildModel(it) }?.let {
            sourceGenerate = sourceGenerate?.substringAfter("<body>") ?: StringUtils.EMPTY

            return when (it) {
                is AnalyzeModel -> it.toHtmlDocument()
                is AnalyzeHttpMethod -> it.toHtmlDocument()
                else -> null
            }?.replace("{source}", sourceGenerate ?: StringUtils.EMPTY)
        } ?: return sourceGenerate
    }

    private fun buildModel(psi: PsiElement): BaseAnalyzeModel? {
        psi.parentOfType<PsiMethod>()?.apply {
            if (nameIdentifier == psi) getHttpMethodModel()?.also { return it.analyze() }

            returnTypeElement?.takeIf { it.textRange.startOffset <= psi.textRange.startOffset && it.textRange.endOffset >= psi.textRange.endOffset }
                ?.also { getHttpMethodModel()?.responseBody?.also { return it.analyze() } }
                ?.also { parseService().parseReturnBaseClass(this)?.also { return it.analyze() } }
        }
        return psi.getAnalyzeModel()
    }
}

/**
 * @this PsiIdentifier有效
 */
@JvmName("getAnalyzeModel")
fun PsiElement.getAnalyzeModel(): AnalyzeModel? {
    if (this is PsiClass && this.isControllerClass()) return null
    var parseBaseClass = project.service<PsiParseJavaService>().parseBaseClass(this.parent, this)
    return parseBaseClass?.takeIf { it is ClassType }?.analyze()
}
