// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimistic.spring.ext

import com.github.optimistic.analyze.model.AnalyzeModel
import com.github.optimistic.analyze.model.isLoopCall
import com.github.optimistic.spring.constant.FieldType
import com.github.optimistic.spring.constant.MAX_ANALYZE_DEPTH
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
    ref: RefClassModel? = null, parent: AnalyzeModel? = null, isRefField: Boolean = false, depth: Int = 0
): AnalyzeModel {
    val model = AnalyzeModel(this, parent)
    // todo 避免解析层级过深，需要加入配置项
    if(depth > MAX_ANALYZE_DEPTH) return model

    if (this.isBase()) return model

    val realRefModel = if (ref.isNull()) this.ref else ref

    // 解析集合
    if (sourceType == FieldType.LIST)
        return model.also { realRefModel?.analyze(null, model, depth = depth + 1)?.let { model.children = listOf(it) } }

    // 解析泛型
    if (realRefModel != null && (source.isRef() || isRefField))
        return realRefModel.analyze(null, model, depth = depth + 1)

    // 通过parent避免循环调用，result -> result || result -> List<Result>
    if (model.isLoopCall()) return model

    model.children = this.source.fields?.map { field: FieldModel ->
        field.classType.analyze(realRefModel, model, this.refField == field, depth + 1).apply {
            this.name = field.realName
            this.remark = field.realRemark
            this.required = field.isRequired
        }
    }?.toList()
    return model
}
