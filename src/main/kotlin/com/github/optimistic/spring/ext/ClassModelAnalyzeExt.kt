// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimistic.spring.ext

import com.github.optimistic.analyze.model.AnalyzeModel
import com.github.optimistic.analyze.model.isLoopCall
import com.github.optimistic.spring.constant.FieldType
import com.github.optimistic.spring.model.*

/**
 * ClassModelAnalyzeExt

 * @author OptimisticGeek
 * @date 2024/1/16
 */
@JvmName("analyze")
fun FieldModel.analyze(): AnalyzeModel {
    return this.classType.analyze().also {
        it.name = this.realName
        it.remark = this.realRemark
        it.required = this.isRequired
    }
}

@JvmName("analyze")
fun RefClassModel.analyze(
    ref: RefClassModel? = null, parent: AnalyzeModel? = null, isRefField: Boolean = false
): AnalyzeModel {

    val model = AnalyzeModel(this, parent)

    if (this.isBase()) return model

    val realRefModel = if (ref.isNull()) this.ref else ref

    if (sourceType == FieldType.LIST)
        return model.also { realRefModel?.analyze(null, model)?.let { model.children = listOf(it) } }

    if (realRefModel != null && (source.isRef() || isRefField)) return realRefModel.analyze(null, model)

    // 通过parent避免循环调用，result -> result || result -> List<Result>
    if (model.isLoopCall()) return model

    model.children = this.source.fields?.map { field: FieldModel ->
        field.classType.analyze(realRefModel, model, this.refField == field).apply {
            this.name = field.realName
            this.remark = field.realRemark
            this.required = field.isRequired
        }
    }?.toList()
    return model
}
