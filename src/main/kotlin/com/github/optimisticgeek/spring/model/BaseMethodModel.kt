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
class BaseMethodModel(val element: UrlMappingElement) {
    val myProject by lazy { psiMethod.project }
    val myModule by lazy { psiClass.let { ModuleUtilCore.findModuleForPsiElement(it)!! } }
    val psiClass get() = psiMethod.containingClass!!
    val psiMethod by lazy { element.navigationTarget as PsiMethod }
    val httpMethod get() = HttpMethodType.valueOf(element.method.first().name)
    val url by lazy { "/${element.presentation}" }
    val position by lazy { psiMethod.getPosition() }
    val name by lazy { position.className() }
    val author by lazy {
        psiMethod.getAuthor().let { if (it.isBlank()) psiClass.getAuthor() else it }
    }
    val remark by lazy {
        psiClass.getRemark().let { if (it.isBlank() || it == psiClass.name) "" else "$it-" } + psiMethod
            .getRemark()
    }

    val isViewer: Boolean by lazy {
        return@lazy psiMethod.hasAnnotation(RESPONSE_BODY)
                || psiClass.hasAnnotation(REST_CONTROLLER) || psiClass.hasAnnotation(RESPONSE_BODY)
    }
    var requestBody: FieldModel? = null
        get() = init().value
    var pathVariables = ArrayList<FieldModel>()
    var queryParams = ArrayList<FieldModel>()
    var responseBody: RefClassModel? = null
        get() {
            init();return field
        }

    fun init() = lazy {
        psiMethod.apply {
            buildParameters(pathVariables, queryParams) { requestBody = it }
            responseBody = buildResponseBody()
        }
        return@lazy requestBody
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BaseMethodModel) return false
        return element == other.element
    }

    override fun hashCode(): Int {
        return element.hashCode()
    }

    override fun toString(): String {
        return "[${httpMethod}]$url $remark"
    }
}
