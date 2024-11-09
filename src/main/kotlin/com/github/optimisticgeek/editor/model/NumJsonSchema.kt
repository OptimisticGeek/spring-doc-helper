package com.github.optimisticgeek.editor.model

import com.github.optimisticgeek.spring.constant.FieldType
import org.apache.commons.lang3.StringUtils.EMPTY

class NumJsonSchema(remark: String? = EMPTY) : AbsJsonSchema(FieldType.NUMBER, remark ?: EMPTY)