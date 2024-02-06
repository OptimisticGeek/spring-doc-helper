// Copyright 2023-2024 79127. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimisticgeek.spring.ext

import com.github.optimisticgeek.spring.constant.AUTHOR
import com.github.optimisticgeek.spring.constant.PARAM
import com.intellij.psi.PsiJavaDocumentedElement
import org.apache.commons.lang3.StringUtils

fun PsiJavaDocumentedElement.getDocumentTitle(): String {
    return this.docComment?.descriptionElements?.getOrNull(1)?.text?.trim() ?: StringUtils.EMPTY
}

fun PsiJavaDocumentedElement.getDocumentTag(tag: String): String {
    return this.docComment?.findTagByName(tag)?.valueElement?.text ?: StringUtils.EMPTY
}

fun PsiJavaDocumentedElement.getDocumentTagParam(param: String): String {
    return this.getDocumentTagParam(PARAM, param)
}

fun PsiJavaDocumentedElement.getDocumentTagParam(tag: String, param: String): String {
    return this.docComment?.findTagsByName(tag)?.map { it.text }
        ?.firstOrNull { it.startsWith("@$tag $param ") }
        ?.replace(Regex("^@$tag $param (.+?)\n.*?$"), "\$1")
        ?: StringUtils.EMPTY
}

fun PsiJavaDocumentedElement.getAuthor(): String {
    return this.getDocumentTag(AUTHOR)
}

fun PsiJavaDocumentedElement.getRemark(): String {
    return this.getDocumentTitle()
}