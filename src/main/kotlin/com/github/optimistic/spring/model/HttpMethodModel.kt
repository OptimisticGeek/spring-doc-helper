// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.github.optimistic.spring.model

import com.github.optimistic.spring.constant.FieldType
import com.github.optimistic.spring.constant.RESPONSE_BODY
import com.github.optimistic.spring.constant.REST_CONTROLLER
import com.github.optimistic.spring.ext.buildParameters
import com.github.optimistic.spring.ext.className
import com.github.optimistic.spring.ext.getAuthor
import com.github.optimistic.spring.ext.getRemark
import com.github.optimistic.spring.model.type.BaseClass
import com.github.optimistic.spring.parse.parseService
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.psi.PsiMethod
import com.intellij.psi.SmartPointerManager
import org.apache.commons.lang3.BooleanUtils

// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

/**
 * BaseUrlModel

 * @author OptimisticGeek
 * @date 2024/2/19
 */
class HttpMethodModel(val element: PsiMethod, val parent: ControllerModel) {
    /**
     * 来源
     */
    val sourcePsi = SmartPointerManager.createPointer<PsiMethod>(element)
    val urlPath: UrlPath = UrlPath(element, parent.urlPath)
    val psiClass get() = psiMethod.containingClass!!
    val psiMethod get() = sourcePsi.element!!
    val myModule get() = psiClass.let { ModuleUtilCore.findModuleForPsiElement(it)!! }
    val httpMethod get() = urlPath.httpMethod
    val url = urlPath.url
    val position by lazy { psiMethod.getPosition() }
    val name by lazy { position.className() }
    val author by lazy { psiMethod.getAuthor().let { it.ifBlank { parent.author ?: "" } } }
    val remark by lazy {
        parent.remark!!.let { if (it.isBlank() || it == psiClass.name) "" else "$it-" } + psiMethod.getRemark()
            .let { it.ifBlank { psiMethod.name } }
    }

    private val params: MethodParams by lazy { MethodParams(psiMethod) }

    val requestBody get() = params.requestBody
    val pathVariables get() = params.pathVariables
    val queryParams get() = params.queryParams
    val responseBody get() = params.responseBody

    val isViewer: Boolean by lazy {
        requestBody?.type == FieldType.STRING && !BooleanUtils.or(
            psiMethod.hasAnnotation(RESPONSE_BODY),
            psiClass.hasAnnotation(REST_CONTROLLER),
            psiClass.hasAnnotation(RESPONSE_BODY)
        )
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
    val requestBody: Field?
    val pathVariables = ArrayList<Field>()
    val queryParams = ArrayList<Field>()
    val responseBody: BaseClass? by lazy { psiMethod.parseService().parseReturnBaseClass(psiMethod)}

    init {
        psiMethod.apply {
            var body: Field? = null
            buildParameters(pathVariables, queryParams) { body = it }
            requestBody = body
        }
    }
}