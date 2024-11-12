// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimistic.editor.listener

import com.github.optimistic.analyze.model.AnalyzeHttpMethod
import com.github.optimistic.analyze.model.AnalyzeModel
import com.github.optimistic.analyze.model.BaseAnalyzeModel
import com.github.optimistic.spring.ext.analyze
import com.github.optimistic.spring.ext.buildResponseBody
import com.github.optimistic.spring.ext.toRefClassModel
import com.github.optimistic.spring.model.toRefClassModel
import com.github.optimistic.spring.service.getHttpMethod
import com.github.optimistic.spring.service.toClassModel
import com.intellij.lang.java.JavaDocumentationProvider
import com.intellij.psi.*
import com.intellij.psi.util.parentOfType

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
        return originalElement?.let { buildModel(it) }.let {
            when (it) {
                is AnalyzeModel -> it.toHtmlDocument()
                is AnalyzeHttpMethod -> it.toHtmlDocument()
                else -> null
            }
        } ?: super.generateDoc(element, originalElement)
    }

    private fun buildModel(originalElement: PsiElement): BaseAnalyzeModel? {
        return originalElement.parentOfType<PsiMethod>()?.takeIf { it.nameIdentifier == originalElement }
            ?.let { it.getHttpMethod()?.analyze() }
            ?: originalElement.getAnalyzeModel()
    }
}

/**
 * @this PsiIdentifier有效
 */
@JvmName("getAnalyzeModel")
fun PsiElement.getAnalyzeModel(): AnalyzeModel? {
    val psiMethod = parentOfType<PsiMethod>()

    when (val current = this.parent) {
        is PsiNewExpression -> current.resolveConstructor()?.analyzeReturnModel()
        is PsiVariable -> current.typeElement?.toRefClassModel()?.analyze()
        is PsiClass -> current.toClassModel()?.toRefClassModel()?.analyze()
        is PsiJavaCodeReferenceElement -> when (val resole = current.resolve()) {
            is PsiVariable -> resole.typeElement?.toRefClassModel()?.analyze()
            is PsiMethod -> {
                if (psiMethod?.returnTypeElement?.text == resole.returnTypeElement?.text)
                    psiMethod.analyzeReturnModel()
                else
                    resole.returnTypeElement?.toRefClassModel()?.analyze()
            }

            else -> current.parentOfType<PsiTypeElement>()?.let {
                if (psiMethod?.returnTypeElement == it)
                    return psiMethod.analyzeReturnModel()
                else
                    it.toRefClassModel()?.analyze()
            }
        }

        else -> null
    }?.takeIf { !it.children.isNullOrEmpty() }?.let { return it }

    psiMethod ?: return null
    when (parent) {
        psiMethod -> psiMethod
        psiMethod.returnTypeElement -> psiMethod
        else -> if (this.text == psiMethod.returnTypeElement?.text) return psiMethod.analyzeReturnModel() else null
    }.analyzeReturnModel()?.takeIf { !it.children.isNullOrEmpty() }.let { return it }
}

@JvmName("analyzeReturnModel")
private fun PsiMethod?.analyzeReturnModel(): AnalyzeModel? = this?.buildResponseBody()?.analyze()

