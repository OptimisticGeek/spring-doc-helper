// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimisticgeek.spring.ext

import com.github.optimisticgeek.spring.constant.AUTHOR
import com.github.optimisticgeek.spring.constant.PARAM
import com.intellij.psi.PsiJavaDocumentedElement

@JvmName("getDocumentTitle")
fun PsiJavaDocumentedElement.getDocumentTitle(): String {
    return this.docComment?.descriptionElements?.getOrNull(1)?.text.trim()
}

@JvmName("getDocumentTag")
fun PsiJavaDocumentedElement.getDocumentTag(tag: String): String {
    return this.docComment?.findTagByName(tag)?.valueElement?.text.trim()
}

@JvmName("getDocumentTagParam")
fun PsiJavaDocumentedElement.getDocumentTagParam(param: String): String {
    return this.getDocumentTagParam(PARAM, param)
}

@JvmName("getDocumentTagParam")
fun PsiJavaDocumentedElement.getDocumentTagParam(tag: String, param: String): String {
    return this.docComment?.findTagsByName(tag)?.map { it.text }
        ?.firstOrNull { it.startsWith("@$tag $param ") }
        ?.replace(Regex("^@$tag $param (.+?)\n.*?$"), "\$1").trim()
}

@JvmName("getAuthor")
fun PsiJavaDocumentedElement.getAuthor(): String {
    return this.getDocumentTag(AUTHOR)
}

@JvmName("getRemark")
fun PsiJavaDocumentedElement.getRemark(): String {
    return this.getDocumentTitle()
}

@JvmName("trim")
private fun String?.trim(): String {
    return this?.replace("\\s*<\\s*\\w+\\s*[^>]*>\\s*", "") ?: ""
}