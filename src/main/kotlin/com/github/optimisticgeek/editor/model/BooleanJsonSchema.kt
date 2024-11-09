package com.github.optimisticgeek.editor.model

import com.github.optimisticgeek.spring.constant.FieldType
import org.apache.commons.lang3.StringUtils.EMPTY

class BooleanJsonSchema(remark: String? = EMPTY) : AbsJsonSchema(FieldType.BOOLEAN, remark ?: EMPTY)