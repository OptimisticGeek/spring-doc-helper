package com.github.optimistic.editor.model

import com.github.optimistic.analyze.model.AnalyzeModel
import com.github.optimistic.spring.constant.FieldType
import com.github.optimistic.spring.constant.FieldType.*
import com.github.optimistic.spring.ext.className

/**
 * @property type 类型名
 * @property description 描述
 * @property defaultValue 默认值
 */
@Suppress("MemberVisibilityCanBePrivate")
abstract class AbsJsonSchema(
    val fieldType: FieldType,
    val description: String,
    val type: String = fieldType.qName.className().lowercase(),
    var defaultValue: Any? = fieldType.defaultValue
)

@JvmName("getJsonSchema")
fun AnalyzeModel.getJsonSchema(): AbsJsonSchema? {
    return when (type) {
        MAP, OBJECT -> ObjJsonSchema(this)
        LIST, ARRAY -> ArrJsonSchema(this)
        LONG, INTEGER -> IntJsonSchema(remark)
        BYTE, DATE, CHAR, ENUM, STRING -> StrJsonSchema(remark)
        NUMBER, DOUBLE -> NumJsonSchema(remark)
        BOOLEAN -> BooleanJsonSchema(remark)
        else -> null
    }
}