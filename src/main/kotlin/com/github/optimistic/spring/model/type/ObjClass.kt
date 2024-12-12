package com.github.optimistic.spring.model.type

import com.github.optimistic.spring.constant.FieldType
import com.github.optimistic.spring.ext.className
import com.github.optimistic.spring.ext.refreshFields
import com.github.optimistic.spring.model.Field
import com.intellij.psi.PsiClass
import com.intellij.psi.SmartPsiElementPointer
import org.apache.commons.collections.CollectionUtils
import org.apache.commons.lang3.StringUtils

/**
 * 对象
 */
open class ObjClass(
    qName: String,
    type: FieldType = FieldType.OBJECT,
    open val fields: MutableSet<Field> = mutableSetOf(),
    open var author: String = StringUtils.EMPTY,
    remark: String = StringUtils.EMPTY
) : BaseRefClass(type, qName, qName.className(), remark = remark) {
    var psi: SmartPsiElementPointer<PsiClass>? = null
    internal var isInit = false

    open val targetFields: MutableList<Field> = mutableListOf()

    open fun refresh(isForce: Boolean = false): ObjClass = this.refreshFields(this.psi?.element, isForce)

    override fun getClassName(target: BaseClass?): String {
        val target = checkTarget(target)
        if (CollectionUtils.isEmpty(targetFields)) return className
        if (CollectionUtils.isNotEmpty(targetFields)) return "$className<${target ?: targetFields.firstOrNull()!!.classType}>"
        return "$className<${target ?: "T"}>"
    }

    fun mergedFields(fields: Collection<Field>) {
        this.fields.stream()
        fields.filter { it.isRequired != null && (!this.fields.contains(it) || it.checkValid()) }
            .forEach { this.fields.add(it) }
    }

    override fun init(target: BaseClass?): ObjClass {
        super.init(target)
        this.refreshTargetFields()
        return this
    }

    fun refreshTargetFields() {
        targetFields.clear()
        targetFields.addAll(this.fields.filter { !it.checkValid() })
    }

    override fun getScore(): Int = fields.size + super.getScore()
}

