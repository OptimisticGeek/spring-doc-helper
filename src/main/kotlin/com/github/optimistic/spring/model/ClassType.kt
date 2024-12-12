package com.github.optimistic.spring.model

import com.github.optimistic.spring.model.type.*
import com.github.optimistic.spring.parse.printAllTrack
import org.apache.commons.lang3.StringUtils

open class ClassType(val source: BaseRefClass) : ObjClass(source.qName, source.type, remark = source.remark) {
    open var parent: ClassType? = null
    protected val fieldMap: MutableMap<String, Field> = mutableMapOf()

    override val fields: MutableSet<Field>
        get() = fieldMap.values.toMutableSet()

    override var target: BaseClass? = null
        get() = source.target.takeIf { it.checkValid() } ?: field ?: source.target

    override var author: String = (source as? ObjClass)?.author ?: StringUtils.EMPTY

    override fun init(target: BaseClass?): ClassType {
        super.init(target)
        return this
    }

    init {
        refresh(true)
        printAllTrack("source不允许使用ClassType: $source", source is ClassType, currentThread = Thread.currentThread())
    }

    override val targetFields: MutableList<Field>
        get() = if (source is ObjClass) source.targetFields else super.targetFields

    override fun getClassName(target: BaseClass?): String {
        if (source !is ObjClass || source is MapClass) return source.getClassName(target)
        return super.getClassName(target)
    }

    fun putField(fieldName: String?, classType: BaseClass?): Boolean {
        if (source !is ObjClass) return false
        classType ?: return false
        fieldName ?: return false
        // 是否为已存在字段，已存在字段只有target有效时覆盖
        fieldMap[fieldName]?.takeIf { it.checkValid() }?.also { return false }
        // 当前字段不存在或无效
        // todo 备注
        Field(fieldName, classType).also { fieldMap[fieldName] = it }
        (classType as? ClassType)?.parent = this
        return true
    }

    /**
     * 刷新target
     */
    override fun refresh(isForce: Boolean): ClassType {
        if (isInit && !isForce) return this
        isInit = true
        source as? ObjClass ?: return this
        fieldMap.clear()
        source.fields.forEach { fieldMap.put(it.name, it) }
        source.fields.mapNotNull { fieldMap.putIfAbsent(it.name, it) }.mapNotNull { it.classType }
            .filterIsInstance<ClassType>().forEach { it.parent = this }.let { return this }
    }

    override fun createList(): ClassType = ClassType(source).init(target).let { ClassType(ListClass()).init(it) }
}