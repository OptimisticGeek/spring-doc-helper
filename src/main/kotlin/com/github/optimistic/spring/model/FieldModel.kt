// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimistic.spring.model

import org.apache.commons.lang3.StringUtils

/**
 * FieldModel

 * @author OptimisticGeek
 * @date 2023/12/27
 */
class FieldModel(
    val fieldName: String, remark: String? = null, var classType: RefClassModel, aliasName: String = fieldName
) : BaseModel(name = fieldName, remark = remark) {
    var isRequired: Boolean = true

    var aliasName: String = aliasName
        get() = if (StringUtils.isBlank(field)) fieldName else field
    val realName: String
        get() = aliasName
    val realRemark: String
        get() = remark ?: classType.remark ?: ""

    override fun toString(): String {
        return "FieldModel(classType=$classType, realName='$realName', realRemark='$realRemark')"
    }
}

