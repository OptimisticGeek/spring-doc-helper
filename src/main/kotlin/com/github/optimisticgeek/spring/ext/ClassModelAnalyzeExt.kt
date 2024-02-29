// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimisticgeek.spring.ext

import com.github.optimisticgeek.analyze.model.AnalyzeModel
import com.github.optimisticgeek.analyze.model.isLoopCall
import com.github.optimisticgeek.spring.constant.FieldType
import com.github.optimisticgeek.spring.model.*

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
    }
}

@JvmName("analyze")
fun RefClassModel.analyze(ref: RefClassModel? = null, parent: AnalyzeModel? = null, isRefField: Boolean = false):
        AnalyzeModel {

    val model = AnalyzeModel(this, parent)

    if (this.isBase()) return model

    val realRefModel = if (ref.isNull()) this.ref else ref

    if (sourceType == FieldType.LIST) {
        return model.also { realRefModel?.analyze(null, model)?.let { model.children = listOf(it) } }
    }

    if (realRefModel != null && (source.isRef() || isRefField)) {
        return realRefModel.analyze(null, model)
    }

    // 通过parent避免循环调用，result -> result || result -> List<Result>
    if (model.isLoopCall()) return model

    model.children = this.source.fields?.map { field: FieldModel ->
        field.classType.analyze(realRefModel, model, this.refField == field).apply {
            this.name = field.realName
            this.remark = field.realRemark
        }
    }?.toList()
    return model
}
