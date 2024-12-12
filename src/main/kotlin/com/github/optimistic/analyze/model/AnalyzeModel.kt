// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimistic.analyze.model

import com.github.optimistic.spring.constant.FieldType
import com.github.optimistic.spring.constant.MAX_ANALYZE_DEPTH
import com.github.optimistic.spring.model.ClassType
import com.github.optimistic.spring.model.Field
import com.github.optimistic.spring.model.type.*
import com.github.optimistic.spring.service.ScannerBundle

class AnalyzeModel(
    val type: FieldType,
    var children: List<AnalyzeModel>? = null,
    val parent: AnalyzeModel? = null,
    val field: Field? = null,
    var required: Boolean = field?.isRequired == true,
    var defaultValue: Any? = type.defaultValue,
    name: String? = field?.aliasName,
    author: String? = null,
    remark: String? = field?.remark,
    position: String? = field?.classType?.qName ?: type.qName
) : BaseAnalyzeModel(name = name, author = author, remark = remark, position = position) {

    /**
     * 基础类型
     */
    constructor(basic: BasicClass, parent: AnalyzeModel?, field: Field? = null) : this(
        basic.type,
        parent = parent,
        field = field,
        defaultValue = basic.defaultValue
    )

    constructor(base: BaseClass, parent: AnalyzeModel?, field: Field? = null) : this(
        base.type,
        parent = parent,
        field = field,
        defaultValue = base.defaultValue
    )

    /**
     * 对象类型
     */
    constructor(obj: ObjClass, parent: AnalyzeModel?, field: Field? = null) : this(
        obj.type,
        parent = parent,
        author = obj.author,
        field = field,
        position = obj.qName
    )

    /**
     * json字段定位($.data、$.rows[0].data)
     */
    val jsonFieldPosition: String
        get() {
            parent ?: return "$"
            return buildString {
                append(parent.jsonFieldPosition)
                if (type == FieldType.LIST) append("[i]")
                if (name != null) append(".$name")
            }
        }
}

/**
 * 循环调用返回true
 */
@JvmName("isLoopCall")
fun AnalyzeModel.getLoopCallAt(): AnalyzeModel? {
    if (type != FieldType.OBJECT || parent == null || this.position.isNullOrBlank()) return null
    var current = this.parent
    while (current != null) {
        if (!current.type.isObj && current.type != FieldType.LIST) return null
        if (current.position == this.position) return current
        current = current.parent
    }
    return null
}

@JvmName("analyzeClass")
fun BaseClass.analyze(
    target: BaseClass? = null,
    parent: AnalyzeModel? = null,
    depth: Int = 0,
    field: Field? = null
): AnalyzeModel {
    if (this !is ClassType) return AnalyzeModel(this as BasicClass, parent, field)

    val realTarget = this.target?.takeIf { it.checkValid() } ?: target ?: this.target

    var model: AnalyzeModel = when (source) {
        // 泛型直接使用真实类型来解析
        is SubstituteClass -> realTarget?.analyze(null, parent, depth, field)?.also { return it }
            ?: AnalyzeModel(source, parent, field)

        is ListClass -> AnalyzeModel(source, parent, field)
            .also { it.children = realTarget?.analyze(null, it, depth = depth)?.let { listOf(it) } }
            .let { return it }

        is ObjClass -> AnalyzeModel(source, parent, field)

        else -> AnalyzeModel(source, parent, field)
    }
    if (source !is ObjClass) return model
    // todo 避免解析层级过深，需要加入配置项
    // 通过parent避免循环调用，result -> result || result -> List<Result>
    model.getLoopCallAt()?.let { at ->
        return model.also { it.remark = ScannerBundle.message("analyze.loopCall.error", at.jsonFieldPosition) }
    }
    if (depth > MAX_ANALYZE_DEPTH)
        return model.also { it.remark = ScannerBundle.message("analyze.maxDepth.error", this.className) }

    // 解析字段
    model.children = fields
        .filter { it.isRequired != null }
        .mapNotNull { it.classType?.analyze(realTarget, model, depth + 1, it) }
        .toList()
    return model
}

/**
 * 解析字段
 */
@JvmName("analyzeModel")
fun Field.analyze(): AnalyzeModel? = this.classType?.analyze(field = this)

