// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimistic.editor.listener

import com.github.optimistic.analyze.model.AnalyzeModel
import com.github.optimistic.editor.model.getJsonSchema
import com.github.optimistic.spring.constant.FieldType
import com.google.gson.Gson

@JvmName("toJson")
fun AnalyzeModel.toJson(showRemark: Boolean = true): String {
    return this.appendField(showRemark = showRemark).toString()
}

@JvmName("toJsonSchema")
fun AnalyzeModel.toJsonSchema(showRemark: Boolean = true): String {
    return Gson().toJson(this.getJsonSchema())
}

@JvmName("appendField")
private fun AnalyzeModel.appendField(
    sb: StringBuilder = StringBuilder(), showRemark: Boolean = true, level: Int = 0, last: Boolean = true
): StringBuilder {
    sb.appendRemark(this, showRemark, level)

    if (level == 0 || sb.last() == '\n') sb.appendSpace(level)
    if (!name.isNullOrBlank()) sb.append("\"$name\": ")

    if (type.isBase) sb.append(type.defaultValue).append(if (!last) "," else "").also { return sb }
    sb.append(if (type == FieldType.LIST) "[" else "{")
    children?.also { sb.appendLine() }?.forEachIndexed { index, field ->
        field.appendField(sb, showRemark, level + 1, index == children!!.size - 1).appendLine()
    }
    sb.appendSpace(level).append(if (type == FieldType.LIST) "]" else "}").takeIf { !last }?.append(",")
    return sb
}

@JvmName("appendSpace")
private fun StringBuilder.appendSpace(level: Int): StringBuilder {
    return this.append("    ".repeat(level))
}

@JvmName("appendRemark")
private fun StringBuilder.appendRemark(model: AnalyzeModel, showRemark: Boolean, level: Int): StringBuilder {
    if (!showRemark || model.remark.isNullOrBlank()) return this
    if (isNotEmpty() && last() != '\n') appendLine()
    return this.appendSpace(level).appendLine("// ${model.remark}")
}