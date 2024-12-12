package com.github.optimistic.spring.model.type

import com.github.optimistic.spring.constant.FieldType
import com.github.optimistic.spring.model.ClassType

/**
 * 集合
 */
class ListClass(type: FieldType = FieldType.LIST) : BaseRefClass(type, type.qName) {

    init {
        target = ClassType(SubstituteClass())
    }

    override fun init(target: BaseClass?): ListClass {
        super.init(target)
        return this
    }

    fun className(): String = getClassName(target)

    override fun getClassName(target: BaseClass?): String = "$className<${checkTarget(target) ?: "T"}>"
}