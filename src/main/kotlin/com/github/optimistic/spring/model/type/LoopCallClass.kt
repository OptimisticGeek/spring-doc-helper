package com.github.optimistic.spring.model.type

import com.github.optimistic.spring.model.ClassType
import com.github.optimistic.spring.model.Field
import org.apache.commons.lang3.StringUtils

/**
 * 循环依赖class
 */
class LoopCallClass(val at: ClassType) : ClassType(at.source) {
    init {
        isInit = true
    }

    override var parent: ClassType? = at.parent
    override val fields: MutableSet<Field> = mutableSetOf()

    override var author: String = StringUtils.EMPTY
        get() = at.author

    override var remark: String = StringUtils.EMPTY
        get() = at.author

    override fun getScore(): Int = 1

    override fun init(target: BaseClass?): LoopCallClass = this

    override fun refresh(isForce: Boolean): LoopCallClass = this

    override fun toString(): String = at.getClassName(target ?: at.target)

    override var defaultValue: Any? = null
        get() = null
}
