// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimisticgeek.spring.ext

import com.github.optimisticgeek.spring.constant.*
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiModifierListOwner
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.containers.orNull
import com.intellij.util.containers.stream
import com.intellij.util.net.HTTPMethod
import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.StringUtils
import java.util.*
import java.util.stream.Collectors

@JvmName("getAnnotationValue")
fun PsiModifierListOwner.getAnnotationValue(qName: String, valueName: String): String {
    return this.getAnnotationValues(qName, valueName).firstOrNull() ?: StringUtils.EMPTY
}

@JvmName("getAnnotationValues")
fun PsiModifierListOwner.getAnnotationValues(qName: String, valueName: String): List<String> {
    return this.getAnnotation(qName)?.getAnnotationValues(valueName) ?: return Collections.emptyList()
}

@JvmName("getAnnotationValue")
fun PsiAnnotation.getAnnotationValue(valueName: String): String {
    return this.getAnnotationValues(valueName).firstOrNull() ?: StringUtils.EMPTY
}

@JvmName("getAnnotationValues")
fun PsiAnnotation.getAnnotationValues(valueName: String): List<String> {
    val attributeValue = this.findAttributeValue(valueName) ?: return Collections.emptyList()
    val values = PsiTreeUtil.findChildrenOfType(attributeValue, PsiLiteralExpression::class.java)
    if (values.isEmpty()) {
        return listOf(attributeValue.text.replace("\"", "").replace("{}", ""))
    }
    return values.stream().map { it.text.replace("\"", "").replace("{}", "") }.collect(Collectors.toList())
}

@JvmName("getHttpMethod")
fun PsiAnnotation.getHttpMethod(): HTTPMethod? {
    if (BooleanUtils.isNotTrue(this.qualifiedName?.endsWith("Mapping"))) {
        return null
    }
    if (BooleanUtils.isNotTrue(this.qualifiedName?.startsWith("org.springframework.web.bind.annotation."))) {
        return null
    }
    return when (this.qualifiedName) {
        GET_MAPPING -> HTTPMethod.GET
        POST_MAPPING -> HTTPMethod.POST
        PUT_MAPPING -> HTTPMethod.PUT
        DELETE_MAPPING -> HTTPMethod.DELETE
        else -> {
            val value = this.getAnnotationValue(METHOD)
            if (StringUtils.isBlank(value)) {
                return null
            }
            return HTTPMethod.values().stream().filter { value.endsWith(it.name) }.findAny().orNull()
        }
    }
}