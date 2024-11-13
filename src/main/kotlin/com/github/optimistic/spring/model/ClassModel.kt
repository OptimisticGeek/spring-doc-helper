// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimistic.spring.model

import com.github.optimistic.spring.constant.FieldType

/**
 * ClassModel
 *
 * @author OptimisticGeek
 * @date 2023/12/27
 */
open class ClassModel(
    qName: String, remark: String? = null, var type: FieldType, var fields: List<FieldModel>? = null
) : BaseModel(position = qName, name = qName.className(), remark = remark) {
    constructor(type: FieldType) : this(type.qName, null, type = type)

    constructor(qName: String, remark: String?, fields: ArrayList<FieldModel>) : this(
        qName, remark, FieldType.OBJECT, fields
    )

    var isInit: Boolean = false

    val qName get() = this.position!!

    val className get() = this.qName.className()

    init {
        isInit = type != FieldType.OBJECT || !fields.isNullOrEmpty()
    }

    fun setDefaultStatus() {
        if (this == type.model) return
        this.position = if (this.position.isNullOrBlank() || !type.isObj) type.qName else this.position!!
        this.name = this.qName.className()
        isInit = type != FieldType.OBJECT
        fields = null
    }
}


@JvmName("isRef")
fun ClassModel?.isRef(): Boolean {
    return this != null && type.isRef
}

@JvmName("hasRefField")
fun ClassModel?.hasRefField(): Boolean {
    return this != null && this.fields?.firstOrNull { it.classType.source.isRef() } != null
}

@JvmName("isNull")
fun ClassModel?.isNull(): Boolean {
    return this == null || type == FieldType.OTHER
}

fun String.packageName(): String {
    if (!this.contains(".")) return this
    return this.substring(0, this.lastIndexOf("."))
}

@JvmName("className")
fun String.className(): String {
    if (!this.contains(".")) return this
    return this.substring(this.lastIndexOf(".") + 1)
}

@JvmName("toRefClassModel")
fun ClassModel.toRefClassModel(ref: RefClassModel? = null): RefClassModel {
    return RefClassModel(this, ref)
}
