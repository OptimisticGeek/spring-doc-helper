package com.github.optimisticgeek.editor.model

import com.github.optimisticgeek.analyze.model.AnalyzeModel
import com.github.optimisticgeek.spring.constant.FieldType
import org.apache.commons.lang3.StringUtils.EMPTY

class ArrJsonSchema(remark: String? = EMPTY, val items: AbsJsonSchema? = null) :
    AbsJsonSchema(FieldType.ARRAY, remark ?: EMPTY) {
    constructor(model: AnalyzeModel) : this(model.remark, model.children?.firstOrNull()?.getJsonSchema())
}