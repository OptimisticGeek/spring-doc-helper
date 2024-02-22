// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimisticgeek.analyze.model

import com.github.optimisticgeek.spring.model.HttpMethodModel
import com.github.optimisticgeek.spring.model.BaseModel

/**
 * BaseAnalyzeModel

 * @author OptimisticGeek
 * @date 2024/1/18
 */
abstract class BaseAnalyzeModel(name: String?, author: String?, remark: String?, position: String?) :
    BaseModel(name, author, remark, position) {
    constructor(model: BaseModel?) : this(model?.name, model?.author, model?.remark ?: model?.name, model?.position)
    constructor(model: HttpMethodModel) : this(model.name, model.author, model.remark, model.position)
}