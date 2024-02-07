// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimisticgeek.analyze.model

import com.github.optimisticgeek.spring.constant.FieldType
import com.github.optimisticgeek.spring.ext.analyze
import com.github.optimisticgeek.spring.model.FieldModel
import com.github.optimisticgeek.spring.model.MethodModel
import com.intellij.util.net.HTTPMethod
import java.util.*

data class AnalyzeMethod(val methodModel: MethodModel) : BaseAnalyzeModel(methodModel) {
    val httpMethod: HTTPMethod = methodModel.requestMethod

    val urls: List<String> = methodModel.urls!!

    val createTime: Date = Date()

    val pathParams: AnalyzeModel? = methodModel.pathVariables.toParams()

    val queryParams: AnalyzeModel? = methodModel.queryParams.toParams()

    val requestBody: AnalyzeModel? = methodModel.requestBody?.analyze()?.also { it.name = null }

    val response: AnalyzeModel? = methodModel.responseBody?.analyze()?.also { it.name = null }
}

/**
 * 仅适用于queryParams与pathParams
 */
private fun List<FieldModel>.toParams(): AnalyzeModel? {
    this.map(FieldModel::analyze)
        .flatMap { if (it.type.isBase) listOf(it) else it.children ?: Collections.emptyList() }.toList()
        .takeIf { it.isNotEmpty() }
        .let { return if (!it.isNullOrEmpty()) AnalyzeModel(FieldType.OBJECT, it) else null }
}
