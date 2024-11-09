package com.github.optimisticgeek.editor.model

import com.github.optimisticgeek.analyze.model.AnalyzeModel
import com.github.optimisticgeek.spring.constant.FieldType
import com.github.optimisticgeek.spring.constant.FieldType.OBJECT
import org.apache.commons.lang.BooleanUtils
import org.apache.commons.lang3.StringUtils.EMPTY

class ObjJsonSchema(
    fieldType: FieldType = OBJECT, remark: String = EMPTY, val author: String = EMPTY, val qName: String = EMPTY
) : AbsJsonSchema(fieldType, remark) {

    constructor(model: AnalyzeModel) : this(
        remark = model.remark ?: EMPTY, author = model.author ?: EMPTY, qName = model.position ?: EMPTY
    ) {
        model.children?.forEach {
            val fieldName = it.name ?: return@forEach
            properties[fieldName] = it.getJsonSchema() ?: return@forEach
            if (BooleanUtils.isTrue(it.required)) required.add(fieldName)
        }
    }


    /**
     * 必填项
     */
    val required: MutableList<String> = mutableListOf()

    /**
     * 所有属性
     */
    val properties: MutableMap<String, AbsJsonSchema> = mutableMapOf()
}
