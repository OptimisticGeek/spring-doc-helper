package com.github.optimistic.editor.model

import com.github.optimistic.analyze.model.AnalyzeModel
import com.github.optimistic.spring.constant.FieldType
import org.apache.commons.lang3.StringUtils.EMPTY

class ArrJsonSchema(remark: String? = EMPTY, val items: AbsJsonSchema? = null) :
    AbsJsonSchema(FieldType.ARRAY, remark ?: EMPTY) {
    constructor(model: AnalyzeModel) : this(model.remark, model.children?.firstOrNull()?.getJsonSchema())
}