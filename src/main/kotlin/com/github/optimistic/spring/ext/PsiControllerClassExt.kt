// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimistic.spring.ext

import com.github.optimistic.spring.constant.*
import com.github.optimistic.spring.model.Field
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.intellij.util.Consumer
import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.StringUtils

@JvmName("buildParameters")
fun PsiMethod.buildParameters(
    pathParams: ArrayList<Field>, queryParams: ArrayList<Field>, requestBody: Consumer<Field>
) {
    if (!this.hasParameters()) return
    this.parameterList.parameters.forEach {
        val fieldModel = Field(it)
        // requestBody
        if (it.hasAnnotation(REQUEST_BODY)) {
            fieldModel.isRequired = true
            requestBody.consume(fieldModel.also { it.name = StringUtils.EMPTY }.also { it.aliasName = StringUtils.EMPTY })
            return@forEach
        }
        // pathVariables
        if (it.hasAnnotation(PATH_VARIABLE)) {
            fieldModel.aliasName = it.getAnnotationValue(PATH_VARIABLE)
            fieldModel.isRequired = true
            pathParams.add(fieldModel)
            return@forEach
        }
        // requestParams
        if (it.hasAnnotation(REQUEST_PARAM)) {
            fieldModel.aliasName = it.getAnnotationValue(REQUEST_PARAM)
            fieldModel.isRequired = it.getAnnotationValue(REQUEST_PARAM, REQUIRED).let { BooleanUtils.toBoolean(it) != false }
        }
        queryParams.add(fieldModel)
    }
}

@JvmName("getHttpRequestAnnotation")
fun PsiMethod.getHttpRequestAnnotation(): PsiAnnotation? {
    return this.getAnnotation(GET_MAPPING) ?: this.getAnnotation(POST_MAPPING) ?: this.getAnnotation(PUT_MAPPING)
    ?: this.getAnnotation(DELETE_MAPPING) ?: this.getAnnotation(REQUEST_MAPPING)
}

@JvmName("isControllerClass")
fun PsiClass.isControllerClass(): Boolean {
    if (this.methods.isEmpty()) {
        return false
    }
    return this.isValid && this.isWritable && (this.hasAnnotation(REST_CONTROLLER) || this.hasAnnotation(CONTROLLER))
}