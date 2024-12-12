package com.github.optimistic.spring.model.type

import com.github.optimistic.spring.constant.FieldType
import com.github.optimistic.spring.ext.refreshFields
import com.github.optimistic.spring.model.Field

/**
 * Map
 */
class MapClass(qName: String) : ObjClass(qName, type = FieldType.MAP) {

    override val fields: MutableSet<Field> = mutableSetOf()

    override fun init(target: BaseClass?): MapClass {
        super.init(target)
        return this
    }

    override fun refresh(isForce: Boolean): MapClass = this.refreshFields(this.psi?.element, isForce)

    override fun getClassName(target: BaseClass?): String {
        val target = checkTarget(target)
        return if (targetFields == null && target == null) className else "$className<String, ${target ?: "Object"}>"
    }
}