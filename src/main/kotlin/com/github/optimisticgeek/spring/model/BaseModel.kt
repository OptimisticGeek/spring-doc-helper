// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimisticgeek.spring.model

/**
 * BaseModel
 * @property position qName or methodPath
 * @property name sourceName
 * @author OptimisticGeek
 * @date 2023/12/27
 */

open class BaseModel(
    var name: String? = null, var author: String? = null, var remark: String? = null, var position: String? = null
) {
    constructor(model: BaseModel? = null) : this(
        model?.name, model?.author, model?.remark ?: model?.name, model?.position
    )
}
