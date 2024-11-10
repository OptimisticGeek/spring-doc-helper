package com.github.optimisticgeek.editor.model

import com.github.optimisticgeek.spring.constant.FieldType
import org.apache.commons.lang3.StringUtils.EMPTY

class StrJsonSchema(remark: String? = EMPTY) : AbsJsonSchema(FieldType.STRING, remark ?: EMPTY)