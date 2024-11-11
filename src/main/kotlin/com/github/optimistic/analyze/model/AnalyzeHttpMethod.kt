// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimistic.analyze.model

import com.github.optimistic.editor.listener.toJson
import com.github.optimistic.spring.constant.FieldType
import com.github.optimistic.spring.constant.HttpMethodType
import com.github.optimistic.spring.ext.analyze
import com.github.optimistic.spring.model.FieldModel
import com.github.optimistic.spring.model.HttpMethodModel
import com.github.optimistic.spring.service.getRootUrl
import com.intellij.openapi.project.Project
import org.apache.commons.lang3.StringUtils
import java.util.*

data class AnalyzeHttpMethod(val httpMethodModel: HttpMethodModel) : BaseAnalyzeModel(httpMethodModel) {
    val httpMethod: HttpMethodType = httpMethodModel.httpMethod

    val urls: List<String> = listOf(httpMethodModel.url)

    val createTime: Date = Date()

    val pathParams: AnalyzeModel? = httpMethodModel.pathVariables.toParams()

    val queryParams: AnalyzeModel? = httpMethodModel.queryParams.toParams()

    val requestBody: AnalyzeModel? = httpMethodModel.requestBody?.analyze()?.also { it.name = null }

    val response: AnalyzeModel? = httpMethodModel.responseBody?.analyze()?.also { it.name = null }

    val project: Project = httpMethodModel.psiMethod.project

    val myModule = httpMethodModel.myModule

    fun getUrl(hasParams: Boolean = true, hasRootUrl: Boolean = false): String = urls.firstOrNull()
        ?.let { if (hasRootUrl) myModule.getRootUrl() + it else it }
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
fun AnalyzeHttpMethod.toCurlStr(): String {
    val sb = StringBuilder("curl -X $httpMethod ")
    requestBody?.let { sb.append(" -H \"Content-Type: application/json\" ") }
        ?.let { sb.append(" -d '${requestBody.toJson(false).replace(Regex("\\s+"), "")}'") }
    response?.let { sb.append(" -H \"Accept: application/json\" ") }
    sb.append(" ${getUrl()}")
    return sb.toString()
}