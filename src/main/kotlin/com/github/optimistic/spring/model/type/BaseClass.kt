package com.github.optimistic.spring.model.type

import com.github.optimistic.spring.constant.FieldType
import com.github.optimistic.spring.model.ClassType
import com.github.optimistic.spring.model.Field
import com.github.optimistic.spring.parse.printDebugLog
import org.apache.commons.collections.CollectionUtils
import org.apache.commons.lang3.StringUtils

/**
 * 抽象class
 *
 * @param type 类型
 * @param className 类名称
 * @param qName 全类名
 */
abstract class BaseClass(
    val type: FieldType,
    val qName: String = type.qName,
    val className: String = type.className(),
    open var remark: String = StringUtils.EMPTY,
    open var defaultValue: Any? = type.defaultValue
) {

    open fun init(target: BaseClass?): BaseClass = this

    override fun toString(): String = className

    override fun equals(other: Any?): Boolean = qName == (other as? BaseClass)?.qName

    override fun hashCode(): Int = type.hashCode() + qName.hashCode()

    /**
     * 计算类权重，用于分辨真实引用类型
     */
    open fun getScore(): Int = if (this is BasicClass) 1 else 0

    open fun createList(): ClassType = ClassType(ListClass().init(this))

    val isValid: Boolean get() = this.checkValid()
}

@JvmName("checkValid")
fun <T : BaseClass?> T?.checkValid(target: BaseClass? = (this as? BaseRefClass)?.target): Boolean {
    this ?: return false
    // 基本类型为有效
    if (this !is BaseRefClass || this is LoopCallClass) return true
    // 检测引用对象是否有效
    if (target.checkValid() || (target != this.target && this.target.checkValid())) return true
    // 引用类型缺失，所以泛型和集合无效
    if (this !is ObjClass) return false
    // 对象有泛型字段
    if(CollectionUtils.isEmpty(targetFields) ) return false
    // 对象类型必须有字段,并且无泛型
    if (fields.isNotEmpty()) return true

    if (this is ClassType) return this.source.checkValid(target)
    return true
}

@JvmName("fieldCheckValid")
fun Field?.checkValid(): Boolean {
    this ?: return false
    classType ?: return false
    isRequired ?: return false
    if (classType !is BaseRefClass) return true
    if (classType !is ClassType) {
        printDebugLog { "未知或不符合规范的字段：$classType" }
        return false
    }
    return classType.checkValid()
}