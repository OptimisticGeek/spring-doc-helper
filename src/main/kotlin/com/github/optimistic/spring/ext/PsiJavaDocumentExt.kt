// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimistic.spring.ext

import com.github.optimistic.spring.constant.*
import com.intellij.psi.PsiJavaDocumentedElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifierListOwner
import com.intellij.psi.PsiVariable
import com.intellij.psi.util.parentOfType
import org.apache.commons.lang3.StringUtils

@JvmName("getDocumentTitle")
private fun PsiJavaDocumentedElement.getDocumentTitle(): String {
    return this.docComment?.descriptionElements?.getOrNull(1)?.text.trimHtmlTag()
}

@JvmName("getDocumentTag")
fun PsiJavaDocumentedElement.getDocumentTag(tag: String): String =
    this.docComment?.findTagByName(tag)?.valueElement?.text.trimHtmlTag()

@JvmName("getDocumentTagParam")
fun PsiJavaDocumentedElement.getDocumentTagParam(param: String): String =
    this.getDocumentTagParam(PARAM, param)

@JvmName("getDocumentTagParam")
fun PsiJavaDocumentedElement.getDocumentTagParam(tag: String, param: String): String {
    return this.docComment?.findTagsByName(tag)?.map { it.text }
        ?.firstOrNull { it.startsWith("@$tag $param") }
        ?.replaceFirst("@$tag $param", "")
        .trimHtmlTag()
}

@JvmName("getAuthor")
fun PsiJavaDocumentedElement.getAuthor(): String = this.getDocumentTag(AUTHOR)

@JvmName("PsiVariableGetDocumentTagParam")
fun PsiVariable.getDocumentTagParam(): String =
    this.name?.let { tag -> this.parentOfType<PsiMethod>()?.getDocumentTag(tag) } ?: StringUtils.EMPTY

/**
 *    todo
 *     @ApiImplicitParams({
 *         @ApiImplicitParam(name = "userId", value = "用户id", dataType = "Integer", dataTypeClass = Integer.class),
 *         @ApiImplicitParam(name = "username", value = "用户名称", dataType = "String", dataTypeClass = String.class),
 *         @ApiImplicitParam(name = "password", value = "用户密码", dataType = "String", dataTypeClass = String.class),
 *         @ApiImplicitParam(name = "mobile", value = "用户手机", dataType = "String", dataTypeClass = String.class)
 *     })
 */
/**
 * 优先使用swagger的文档，其次使用java原生文档
 */
@JvmName("getRemark")
fun PsiJavaDocumentedElement.getRemark(): String =
    (this as? PsiModifierListOwner)?.getSwaggerRemark()?.takeIf { it.isNotBlank() } ?: getDocumentTitle()

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
private fun String?.trimHtmlTag(): String =
    this?.replace("\\s*<\\s*\\w+\\s*[^>]*>\\s*", "")?.replace("*", " ")?.trim() ?: StringUtils.EMPTY