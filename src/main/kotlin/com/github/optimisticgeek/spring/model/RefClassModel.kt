// Copyright 2023-2024 79127. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimisticgeek.spring.model

import com.github.optimisticgeek.spring.constant.FieldType
import com.github.optimisticgeek.spring.constant.FieldType.*
import org.apache.commons.lang3.StringUtils

/**
 * RefClassModel

 * @author OptimisticGeek
 * @date 2023/12/27
 */
class RefClassModel(val source: ClassModel, var ref: RefClassModel? = null) : BaseModel() {
    val sourceType: FieldType = source.type
    val refType: FieldType? get() = ref?.sourceType

    val realRemark: String
        get() {
            val sourceRemark = source.remark ?: remark
            val refRemark = ref?.remark ?: return sourceRemark ?: StringUtils.EMPTY
            return "${sourceRemark ?: ""}[$refRemark]"
        }

    override fun toString(): String {
        return fullClassName()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RefClassModel) return false

        if (source != other.source) return false
        if (ref != other.ref) return false

        return true
    }

    override fun hashCode(): Int {
        var result = source.hashCode()
        result = 31 * result + (ref?.hashCode() ?: 0)
        return result
    }

    val refField: FieldModel?
        get() = source.fields?.firstOrNull { it.classType.isRef() || it.classType.isObj() }
}

fun RefClassModel.updateRef(newRef: RefClassModel?): RefClassModel {
    newRef ?: return this
    if (ref == null || newRef.getScore() > ref.getScore()) ref = newRef
    // 例如：构造方法为Result(List<T> list)，rows字段为List<T>，如果不做下方处理会造成rows为 List<List<T>>
    if(refField?.classType?.sourceType == LIST && ref?.sourceType == LIST) ref = ref?.ref
    return this
}

private fun RefClassModel?.getScore(): Int {
    if (this == null) return 1
    // 泛型不是真实类型没有作用
    if (sourceType == SUBSTITUTE) return 0
    if (sourceType.isBase) return 2
    return ref.getScore() * 10
}

fun RefClassModel?.isNull(): Boolean {
    return this == null || source.isNull()
}

fun RefClassModel.isRef(): Boolean {
    return source.isRef()
}

fun RefClassModel.isBase(): Boolean {
    return sourceType.isBase
}

fun RefClassModel.isObj(): Boolean {
    return sourceType.isObj
}

fun RefClassModel?.fullClassName(): String {
    this?.source ?: return StringUtils.EMPTY
    return when {
        isBase() || ref == null -> source.className
        sourceType == MAP -> "${source.className}<String, ${ref.fullClassName()}>"
        (sourceType.isRef || sourceType.isObj) -> "${source.className}<${ref.fullClassName()}>"
        else -> source.className
    }
}