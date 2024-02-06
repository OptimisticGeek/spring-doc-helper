// Copyright 2023-2024 79127. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimisticgeek.spring.model

import com.github.optimisticgeek.spring.constant.FieldType
import com.github.optimisticgeek.spring.service.ScannerBundle

/**
 * ClassModel
 *
 * @author OptimisticGeek
 * @date 2023/12/27
 */
open class ClassModel(
    qName: String, remark: String? = null, val type: FieldType, var fields: ArrayList<FieldModel>? = null
) : BaseModel(
    position = qName, remark = remark
) {
    constructor(type: FieldType) : this(type.qName, null, type = type)

    constructor(qName: String, remark: String?, fields: ArrayList<FieldModel>) : this(
        qName, remark, FieldType.OBJECT, fields
    )

    var isInit: Boolean = false

    val qName: String
        get() = this.position!!

    val className: String
        get() = this.qName.className()

    init {
        setDefaultStatus()
    }

    fun setDefaultStatus() {
        if (fields.isNullOrEmpty() && type == FieldType.OBJECT) fields = ArrayList() else isInit = true
        this.position = if (this.position.isNullOrBlank() || !type.isObj) type.qName else this.position!!
        this.name = this.qName.className()
        if (type == FieldType.MAP) {
            fields = arrayListOf(
                FieldModel(
                    fieldName = "data",
                    remark = ScannerBundle.message("scanner.map.key.remark"),
                    classType = RefClassModel(ClassModel(FieldType.SUBSTITUTE))
                )
            )
        }
    }

    override fun toString(): String {
        return "ClassModel(qualifiedName=$qName, fields=$fields)"
    }
}


fun ClassModel?.isRef(): Boolean {
    return this != null && type.isRef
}

fun ClassModel?.hasRefField(): Boolean {
    return this != null && this.fields?.firstOrNull { it.classType.source.isRef() } != null
}

fun ClassModel?.isNull(): Boolean {
    return this == null || type == FieldType.OTHER
}

fun String.// Copyright 2023-2024 79127. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
packageName(): String {
    if (!this.contains(".")) return this
    return this.substring(0, this.lastIndexOf("."))
}

fun String.className(): String {
    if (!this.contains(".")) return this
    return this.substring(this.lastIndexOf(".") + 1)
}

fun ClassModel.toRefClassModel(): RefClassModel {
    return RefClassModel(this)
}

fun ClassModel.toRefClassModel(ref: RefClassModel?): RefClassModel {
    return RefClassModel(this, ref)
}
