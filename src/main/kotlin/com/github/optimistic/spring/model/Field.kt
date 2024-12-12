package com.github.optimistic.spring.model

import com.github.optimistic.spring.constant.*
import com.github.optimistic.spring.ext.*
import com.github.optimistic.spring.model.type.BaseClass
import com.github.optimistic.spring.model.type.LoopCallClass
import com.github.optimistic.spring.parse.parseService
import com.intellij.psi.*
import org.apache.commons.lang3.StringUtils

/**
 * 字段
 */
class Field(
    val sourcePsi: SmartPsiElementPointer<PsiVariable>? = null,
    val type: FieldType = sourcePsi?.element?.typeElement?.let { getFieldType(it.resolvePsiClass(), it.text) }
        ?: FieldType.OTHER,
    classType: BaseClass? = null
) {

    /**
     * 字段名
     */
    var name: String = StringUtils.EMPTY

    /**
     * 别名
     */
    var aliasName: String = StringUtils.EMPTY
        get() = if (StringUtils.isBlank(field)) name else field

    /**
     * 是否必填
     */
    var isRequired: Boolean? = false

    /**
     * 字段类型 (不允许使用，避免栈溢出)
     */
    private val riskyClassType: BaseClass? = classType ?: sourcePsi?.element?.let {
        it.project.parseService().parseVariableBaseClass(it)
    }


    /**
     * 字段类型
     */
    val classType: BaseClass? by lazy {
        if (!isLoopCall) riskyClassType
        else riskyClassType?.let { it as ClassType }?.let { LoopCallClass(it) }
    }

    /**
     * 字段备注
     */
    var remark: String = StringUtils.EMPTY

    /**
     * 检测当前对象是否存在栈溢出的风险
     */
    val isLoopCall: Boolean = checkLoopCall()

    constructor(
        fieldName: String, classType: BaseClass, remark: String = StringUtils.EMPTY
    ) : this(type = classType.type, classType = classType) {
        this.name = fieldName
        this.remark = remark
        this.refresh()
    }

    constructor(
        psi: PsiVariable, remark: String = psi.getDocumentTagParam()
    ) : this(SmartPointerManager.createPointer<PsiVariable>(psi)) {
        this.remark = remark
        this.refresh()
    }

    constructor(psi: PsiField) : this(SmartPointerManager.createPointer<PsiVariable>(psi)) {
        this.refresh()
    }

    fun refresh() {
        when (val psi = sourcePsi?.element) {
            is PsiField -> {
                this.name = psi.name
                this.remark = psi.getRemark()
                this.aliasName = listOf(
                    psi.getAnnotationValue(JACKSON_JSON_FIELD),
                    psi.getAnnotationValue(FASTJSON2_JSON_FIELD, NAME),
                    psi.getAnnotationValue(FASTJSON_JSON_FIELD, NAME)
                ).firstOrNull { it.isNotEmpty() } ?: StringUtils.EMPTY

                this.isRequired = if (classType == null) null else psi.checkRequired()
            }

            is PsiParameter -> {
                this.name = psi.name
                // todo 设置别名，RequestMapping、JsonField之类的
                this.aliasName = this.name
                this.remark = psi.getDocumentTagParam().takeIf { it.isNotEmpty() } ?: remark
                this.isRequired = if (classType == null) null else psi.checkRequired()
            }

            null -> {
                this.isRequired = classType?.takeIf { it.type != FieldType.OTHER }?.let { true }
            }
        }
    }

    override fun toString(): String {
        return buildString {
            if (isRequired == true) append("* ")
            append("$name : $classType")
            if (StringUtils.isNotBlank(remark)) append(" -【$remark】")
        }
    }

    override fun equals(other: Any?): Boolean = name == (other as? Field)?.name

    override fun hashCode(): Int = name.hashCode()

    fun checkLoopCall(): Boolean {
        classType as? ClassType ?: return false
        var parent = (classType as? ClassType)?.parent ?: return false
        while (true) {
            parent.source as? ClassType ?: return false
            if (parent.qName == classType!!.qName) return true
            parent = parent.parent ?: return false
        }
    }
}
