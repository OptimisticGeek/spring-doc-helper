// Copyright 2023-2024 79127. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimisticgeek.spring.model

import org.apache.commons.lang3.StringUtils

/**
 * FieldModel

 * @author OptimisticGeek
 * @date 2023/12/27
 */
class FieldModel(
    val fieldName: String, remark: String? = null, var classType: RefClassModel
) : BaseModel(name = fieldName, remark = remark) {

    var isRequired: Boolean = false
    var aliasName: String? = null
        get() = if (StringUtils.isBlank(field)) fieldName else field
    val realName: String
        get() = aliasName ?: fieldName
    val realRemark: String
        get() = classType.remark ?: remark ?: ""

    override fun toString(): String {
        return "FieldModel(classType=$classType, realName='$realName', realRemark='$realRemark')"
    }
}