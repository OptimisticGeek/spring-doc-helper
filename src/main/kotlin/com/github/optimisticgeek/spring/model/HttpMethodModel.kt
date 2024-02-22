// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.github.optimisticgeek.spring.model

import com.github.optimisticgeek.spring.constant.HttpMethodType
import com.github.optimisticgeek.spring.constant.REST_CONTROLLER
import com.github.optimisticgeek.spring.ext.buildParameters
import com.github.optimisticgeek.spring.ext.buildResponseBody
import com.github.optimisticgeek.spring.ext.getAuthor
import com.github.optimisticgeek.spring.ext.getRemark
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.psi.PsiMethod
import com.intellij.spring.mvc.SpringMvcConstants.RESPONSE_BODY
import com.intellij.spring.mvc.mapping.UrlMappingElement

// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

/**
 * BaseUrlModel

 * @author OptimisticGeek
 * @date 2024/2/19
 */
class HttpMethodModel(val element: UrlMappingElement) {
    val psiClass get() = psiMethod.containingClass!!
    val psiMethod get() = element.navigationTarget as PsiMethod
    val myModule get() = psiClass.let { ModuleUtilCore.findModuleForPsiElement(it)!! }
    val httpMethod get() = HttpMethodType.valueOf(element.method.first().name)
    val url by lazy { "/${element.presentation}" }
    val position by lazy { psiMethod.getPosition() }
    val name by lazy { position.className() }
    val author by lazy { psiMethod.getAuthor().let { it.ifBlank { psiClass.getAuthor() } } }
    val remark by lazy {
        psiClass.getRemark().let { if (it.isBlank() || it == psiClass.name) "" else "$it-" } + psiMethod
            .getRemark().let { it.ifBlank { psiMethod.name } }
    }

    private val params: MethodParams by lazy { MethodParams(psiMethod) }

    val requestBody get() = params.requestBody
    val pathVariables get() = params.pathVariables
    val queryParams get() = params.queryParams
    val responseBody get() = params.responseBody

    val isViewer: Boolean by lazy {
        return@lazy psiMethod.hasAnnotation(RESPONSE_BODY)
                || psiClass.hasAnnotation(REST_CONTROLLER) || psiClass.hasAnnotation(RESPONSE_BODY)
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HttpMethodModel) return false
        return element == other.element
    }

    override fun hashCode(): Int {
        return element.hashCode()
    }

    override fun toString(): String {
        return "[${httpMethod}]$url $remark params=[$params]"
    }
}

@JvmName("getPosition")
fun PsiMethod.getPosition(): String {
    return "${this.containingClass!!.qualifiedName}#$name"
}

private class MethodParams(psiMethod: PsiMethod) {
    val requestBody: FieldModel?
    val pathVariables = ArrayList<FieldModel>()
    val queryParams = ArrayList<FieldModel>()
    val responseBody: RefClassModel? = psiMethod.buildResponseBody()

    init {
        psiMethod.apply {
            var body: FieldModel? = null
            buildParameters(pathVariables, queryParams) { body = it }
            requestBody = body
        }
    }
}