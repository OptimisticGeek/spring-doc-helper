package com.github.optimisticgeek.editor.model

import com.github.optimisticgeek.analyze.model.AnalyzeModel
import com.github.optimisticgeek.spring.constant.FieldType
import com.github.optimisticgeek.spring.constant.FieldType.*
import com.github.optimisticgeek.spring.model.className

/**
 * @property type 类型名
 * @property description 描述
 * @property default 默认值
 */
@Suppress("MemberVisibilityCanBePrivate")
abstract class AbsJsonSchema(
    val fieldType: FieldType,
    val description: String,
    val type: String = fieldType.qName.className().lowercase(),
    var default: Any? = fieldType.defaultValue
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