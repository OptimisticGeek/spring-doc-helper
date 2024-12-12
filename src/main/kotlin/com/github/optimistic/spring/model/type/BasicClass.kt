package com.github.optimistic.spring.model.type

import com.github.optimistic.spring.constant.FieldType

/**
 * java基础类型
 */
open class BasicClass(type: FieldType) : BaseClass(type, type.qName, type.className()) {
    override fun init(target: BaseClass?): BasicClass {
        super.init(target)
        return this
    }
}