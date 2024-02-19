// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimisticgeek.spring.ext

import com.github.optimisticgeek.spring.constant.*
import com.intellij.psi.PsiJavaDocumentedElement
import com.intellij.psi.PsiModifierListOwner
import org.apache.commons.lang3.StringUtils

@JvmName("getDocumentTitle")
private fun PsiJavaDocumentedElement.getDocumentTitle(): String {
    return this.docComment?.descriptionElements?.getOrNull(1)?.text.trimHtmlTag()
}

@JvmName("getDocumentTag")
fun PsiJavaDocumentedElement.getDocumentTag(tag: String): String {
    return this.docComment?.findTagByName(tag)?.valueElement?.text.trimHtmlTag()
}

@JvmName("getDocumentTagParam")
fun PsiJavaDocumentedElement.getDocumentTagParam(param: String): String {
    return this.getDocumentTagParam(PARAM, param)
}

@JvmName("getDocumentTagParam")
fun PsiJavaDocumentedElement.getDocumentTagParam(tag: String, param: String): String {
    return this.docComment?.findTagsByName(tag)?.map { it.text }
        ?.firstOrNull { it.startsWith("@$tag $param") }
        ?.replaceFirst("@$tag $param", "")
        .trimHtmlTag()
}

@JvmName("getAuthor")
fun PsiJavaDocumentedElement.getAuthor(): String {
    return this.getDocumentTag(AUTHOR)
}

@JvmName("getRemark")
fun PsiJavaDocumentedElement.getRemark(): String {
    // 优先使用swagger的文档
    if (this is PsiModifierListOwner) getSwaggerRemark()?.takeIf { it.isNotBlank() }?.let { return it }
    return this.getDocumentTitle()
}

@JvmName("getSwaggerRemark")
fun PsiModifierListOwner.getSwaggerRemark(): String? {
    val swaggerModel = getAnnotation(SWAGGER_API)
        ?: getAnnotation(SWAGGER_METHOD)
        ?: getAnnotation(SWAGGER_FIELD)
        ?: getAnnotation(SWAGGER_MODEL) ?: return null
    swaggerModel.getAnnotationValue(DESCRIPTION).takeIf { it.isNotBlank() }?.let { return it }
    swaggerModel.getAnnotationValue(DEFAULT).takeIf { it.isNotBlank() }?.let { return it }
    return null
}

@JvmName("trimHtmlTag")
private fun String?.trimHtmlTag(): String {
    this ?: return StringUtils.EMPTY
    return this.replace("\\s*<\\s*\\w+\\s*[^>]*>\\s*", "").replace("*", " ").trim()
}