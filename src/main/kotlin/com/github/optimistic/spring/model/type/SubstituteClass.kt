package com.github.optimistic.spring.model.type

import com.github.optimistic.spring.constant.FieldType

/**
 * 引用
 */
open class SubstituteClass(qName: String = "T") :
    BaseRefClass(FieldType.SUBSTITUTE, if (qName == "?") "T" else if (qName == FieldType.OBJECT.qName) "T" else qName) {

    override fun init(target: BaseClass?): SubstituteClass {
        super.init(target)
        return this
    }

    override fun getClassName(target: BaseClass?): String = checkTarget(target)?.toString() ?: className
}
