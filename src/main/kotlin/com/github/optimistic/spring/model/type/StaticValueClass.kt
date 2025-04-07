package com.github.optimistic.spring.model.type

import com.github.optimistic.spring.constant.FieldType

class StaticValueClass(value: Any, type: FieldType = FieldType.STRING) : BasicClass(type) {
    override var defaultValue: Any? = value
        get() = field ?: super.defaultValue

    override fun toString(): String {
        return "value: ${defaultValue?.toString() ?: super.toString()}"
    }
}