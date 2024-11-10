// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimisticgeek.editor.listener

import com.github.optimisticgeek.analyze.model.AnalyzeHttpMethod
import com.github.optimisticgeek.analyze.model.AnalyzeModel
import com.github.optimisticgeek.analyze.model.BaseAnalyzeModel
import com.github.optimisticgeek.spring.ext.*
import com.github.optimisticgeek.spring.service.getHttpMethod
import com.intellij.lang.java.JavaDocumentationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.PsiVariable
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
        buildModel(element, originalElement).also {
            return when (it) {
                is AnalyzeModel -> if (it.children.isNullOrEmpty())
                    super.generateDoc(element, originalElement) else it.toHtmlDocument()

                is AnalyzeHttpMethod -> it.toHtmlDocument()
                else -> super.generateDoc(element, originalElement)
            }
        }
    }

    private fun buildModel(element: PsiElement?, originalElement: PsiElement?): BaseAnalyzeModel? {
        // 普通方法的返回值 通过分析获取
        val psiMethod = originalElement?.parentOfType<PsiMethod>()
        psiMethod?.takeIf { it.returnTypeElement?.textOffset == originalElement.textOffset }
            ?.let {
                return originalElement.parentOfType<PsiMethodCallExpression>()?.analyzeResponseBody()?.analyze()
                    ?: it.buildResponseBody()?.analyze()
            }

        psiMethod?.getHttpRequestAnnotation()?.getHttpMethodType() ?: return null
        return when (element) {
            // Api接口 或者 普通方法的返回值
            is PsiMethod -> {
                element.getHttpMethod()?.analyze()
                    ?: originalElement.parentOfType<PsiMethodCallExpression>()?.analyzeResponseBody()?.analyze()
                    ?: element.buildResponseBody()?.analyze()
            }

//            is PsiClass -> {
//                if (element.isControllerClass()) null else element.toClassModel()?.toRefClassModel()
//                    ?.analyze()
//            }

            is PsiVariable -> element.toRefClassModel()?.analyze()
            else -> null
        }
    }
}


