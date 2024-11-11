package com.github.optimistic.editor.model

import com.github.optimistic.spring.constant.FieldType
import org.apache.commons.lang3.StringUtils.EMPTY

class NumJsonSchema(remark: String? = EMPTY) : AbsJsonSchema(FieldType.NUMBER, remark ?: EMPTY)