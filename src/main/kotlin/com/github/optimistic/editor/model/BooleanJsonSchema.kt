package com.github.optimistic.editor.model

import com.github.optimistic.spring.constant.FieldType
import org.apache.commons.lang3.StringUtils.EMPTY

class BooleanJsonSchema(remark: String? = EMPTY) : AbsJsonSchema(FieldType.BOOLEAN, remark ?: EMPTY)