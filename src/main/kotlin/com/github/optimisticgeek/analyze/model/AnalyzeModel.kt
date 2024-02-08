// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimisticgeek.analyze.model

import com.github.optimisticgeek.spring.constant.FieldType
import com.github.optimisticgeek.spring.model.BaseModel
import com.github.optimisticgeek.spring.model.RefClassModel

class AnalyzeModel(
    val type: FieldType,
    var children: List<AnalyzeModel>? = null,
    model: BaseModel? = null,
    val parent: AnalyzeModel? = null
) : BaseAnalyzeModel(model) {
    constructor(ref: RefClassModel, parent: AnalyzeModel?) : this(ref.sourceType, null, ref.source, parent) {
        this.remark = ref.remark ?: ref.realRemark
    }

    init {
        this.name = null
    }
}

/**
 * 循环调用返回true
 */
@JvmName("isLoopCall")
fun AnalyzeModel.isLoopCall(): Boolean {
    if (type != FieldType.OBJECT || parent == null || this.position.isNullOrBlank()) return false
    var current = this.parent
    while (current != null) {
        if (!current.type.isObj && current.type != FieldType.LIST) return false
        if (current.position == this.position) return true
        current = current.parent
    }
    return false
}
