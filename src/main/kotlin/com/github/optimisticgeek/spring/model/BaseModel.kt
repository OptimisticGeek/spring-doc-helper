package com.github.optimisticgeek.spring.model

/**
 * BaseModel
 * @property position qName or methodPath
 * @property name sourceName
 * @author OptimisticGeek
 * @date 2023/12/27
 */

open class BaseModel(
    internal var name: String? = null,
    var author: String? = null,
    var remark: String? = null,
    internal var position: String? = null
) {
    constructor(model: BaseModel?) : this(model?.name, model?.author, model?.remark ?: model?.name, model?.position)

    override fun toString(): String {
        return "BaseModel(name=$name, author=$author, remark=$remark, position=$position"
    }
}
