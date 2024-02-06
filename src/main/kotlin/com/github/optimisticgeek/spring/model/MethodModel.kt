// Copyright 2023-2024 79127. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimisticgeek.spring.model

import com.github.optimisticgeek.spring.ext.getAuthor
import com.github.optimisticgeek.spring.ext.getHttpRequestAnnotation
import com.github.optimisticgeek.spring.ext.getRemark
import com.github.optimisticgeek.spring.ext.getRequestMethod
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiMethod
import com.intellij.spring.mvc.jam.RequestMethod
import org.apache.commons.lang3.StringUtils

/**
 * MethodModel

 * @author OptimisticGeek
 * @date 2023/12/27
 */
class MethodModel(val psiMethod: PsiMethod) : BaseModel(
    psiMethod.name, psiMethod.getAuthor(), psiMethod.getRemark(), psiMethod.getPosition()
) {
    val psiMethodAnnotation: PsiAnnotation = psiMethod.getHttpRequestAnnotation()!!
    val requestMethod: RequestMethod = psiMethodAnnotation.getRequestMethod() ?: RequestMethod.GET
    val methodName: String = name!!
    var urls: List<String>? = null
    var isViewer: Boolean? = null
    var requestBody: FieldModel? = null
    val pathVariables = ArrayList<FieldModel>()
    val queryParams = ArrayList<FieldModel>()
    var responseBody: RefClassModel? = null

    // todo 暂未增加对viewer跳转的识别
    fun getUrl(): String {
        return urls?.firstOrNull() ?: StringUtils.EMPTY
    }

    override fun toString(): String {
        return "MethodModel(urls=$urls, isViewer=$isViewer, requestMethod=$requestMethod, psiMethodAnnotation=$psiMethodAnnotation, requestBody=$requestBody, pathVariables=$pathVariables, requestParams=$queryParams, responseBody=$responseBody)"
    }
}

fun PsiMethod.getPosition(): String {
    return "${this.containingClass!!.qualifiedName}#$name"
}