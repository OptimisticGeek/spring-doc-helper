package com.github.optimistic.spring.model.type

import com.github.optimistic.spring.constant.FieldType

/**
 * 表单文件
 */
class MultipartFileClass(type: FieldType = FieldType.MULTIPART_FILE) : BaseClass(type, type.qName, type.className()) {
    override fun init(target: BaseClass?): MultipartFileClass {
        super.init(target)
        return this
    }
}