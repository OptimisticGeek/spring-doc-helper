// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimisticgeek.analyze.model

import com.github.optimisticgeek.editor.listener.toJson
import com.github.optimisticgeek.spring.constant.FieldType
import com.github.optimisticgeek.spring.constant.HttpMethodType
import com.github.optimisticgeek.spring.ext.analyze
import com.github.optimisticgeek.spring.model.MethodModel
import com.github.optimisticgeek.spring.model.FieldModel
import com.intellij.openapi.project.Project
import org.apache.commons.lang3.StringUtils
import java.util.*

data class AnalyzeMethod(val methodModel: MethodModel) : BaseAnalyzeModel(methodModel) {
    val httpMethod: HttpMethodType = methodModel.httpMethod

    val urls: List<String> = listOf(methodModel.url)

    val createTime: Date = Date()

    val pathParams: AnalyzeModel? = methodModel.pathVariables.toParams()

    val queryParams: AnalyzeModel? = methodModel.queryParams.toParams()

    val requestBody: AnalyzeModel? = methodModel.requestBody?.analyze()?.also { it.name = null }

    val response: AnalyzeModel? = methodModel.responseBody?.analyze()?.also { it.name = null }

    val project: Project = methodModel.psiMethod.project

    fun getUrl(hasParams: Boolean = true): String = urls.firstOrNull()
        ?.let { if (hasParams) it.replaceUrlPathParams(pathParams).joinQueryParams(queryParams) else it }
        ?: StringUtils.EMPTY
}

@JvmName("replaceUrlPathParams")
private fun String.replaceUrlPathParams(pathParams: AnalyzeModel?): String {
    var url = this
    pathParams?.children
        ?.forEach { url = url.replace(Regex("(\\{${it.name}(:\\S+)*})"), it.type.getDefaultValue()) }
        .let { return url }
}

@JvmName("joinQueryParams")
private fun String.joinQueryParams(queryParams: AnalyzeModel?): String =
    queryParams?.children?.joinToString("&") { "${it.name}=${it.type.getDefaultValue()}" }?.let { "$this?$it" } ?: this

/**
 * 仅适用于queryParams与pathParams
 */
@JvmName("toParams")
private fun List<FieldModel>.toParams(): AnalyzeModel? = this.map(FieldModel::analyze)
    .flatMap { if (!it.type.isObj) listOf(it) else it.children ?: Collections.emptyList() }.toList()
    .takeIf { it.isNotEmpty() }
    .let { return if (!it.isNullOrEmpty()) AnalyzeModel(FieldType.OBJECT, it) else null }

@JvmName("toCurlStr")
fun AnalyzeMethod.toCurlStr(): String {
    val sb = StringBuilder("curl -X $httpMethod ")
    requestBody?.let { sb.append(" -H \"Content-Type: application/json\" ") }
        ?.let { sb.append(" -d '${requestBody.toJson(false).replace(Regex("\\s+"), "")}'") }
    response?.let { sb.append(" -H \"Accept: application/json\" ") }
    sb.append(" ${getUrl()}")
    return sb.toString()
}