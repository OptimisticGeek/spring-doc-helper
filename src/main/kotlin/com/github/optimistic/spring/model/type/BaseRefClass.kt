package com.github.optimistic.spring.model.type

import com.github.optimistic.spring.constant.FieldType
import com.github.optimistic.spring.ext.className
import com.github.optimistic.spring.model.ClassType
import org.apache.commons.lang3.StringUtils

/**
 * 抽象class
 *
 * @param type 类型
 * @param className 类名称
 * @param qName 全类名
 */
abstract class BaseRefClass(
    type: FieldType,
    qName: String = type.qName,
    className: String = qName.className(),
    remark: String = StringUtils.EMPTY
) : BaseClass(type, qName, className, remark = remark) {
    open var target: BaseClass? = null

    override fun init(target: BaseClass?): BaseRefClass {
        target ?: return this
        this.target = target
        return this
    }

    /**
     * 查找真实的target
     */
    fun checkTarget(target: BaseClass? = this.target): BaseClass? = this.target?.takeIf { it.checkValid() } ?: target

    override fun toString(): String = getClassName()

    /**
     * 自身权重
     */
    override fun getScore(): Int = (if (checkValid()) 1 else 0) + getTargetScore()

    /**
     * target权重
     */
    internal fun getTargetScore(): Int = target?.getScore() ?: 0

    override fun createList(): ClassType = ClassType(this).init(target)

    open fun getClassName(target: BaseClass? = this.target): String = "$className<${checkTarget(target) ?: "T"}>"
}

