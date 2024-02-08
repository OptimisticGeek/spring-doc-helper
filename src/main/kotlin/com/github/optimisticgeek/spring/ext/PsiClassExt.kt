// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimisticgeek.spring.ext

import com.github.optimisticgeek.spring.constant.FieldType
import com.github.optimisticgeek.spring.constant.StringConstants.ARRAY_TAG
import com.github.optimisticgeek.spring.constant.StringConstants.VOID
import com.github.optimisticgeek.spring.model.ClassModel
import com.github.optimisticgeek.spring.model.FieldModel
import com.github.optimisticgeek.spring.model.RefClassModel
import com.github.optimisticgeek.spring.model.toRefClassModel
import com.github.optimisticgeek.spring.service.scannerService
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import java.util.*

@JvmName("toClassModel")
fun PsiClass.toClassModel(useCache: Boolean = true): ClassModel? {

    val type = FieldType.getType(this, this.qualifiedName ?: this.name).takeIf { it != FieldType.OTHER } ?: return null

    val source = this.scannerService().buildSourceModel(this.qualifiedName ?: this.text, type, useCache)
    if (source.isInit) return source

    synchronized(source) {
        source.isInit = true
        source.author = this.getAuthor()
        source.remark = this.getRemark().let { it.ifBlank { source.remark } }
        if (source.type == FieldType.OBJECT) source.fields!!.addAll(this.fields())
        return source
    }
}

@JvmName("fields")
fun PsiClass.fields(): List<FieldModel> {
    return this.allFields.filter { !(it.hasModifierProperty(PsiModifier.FINAL) || it.hasModifierProperty(PsiModifier.STATIC)) }
        .mapNotNull {
            val refClassModel = it.typeElement?.toRefClassModel() ?: return@mapNotNull null
            FieldModel(it.name, it.getRemark(), refClassModel).also { "${this.qualifiedName}#${it.name}" }
        }.toList()
}

@JvmName("findClassModels")
fun PsiTypeElement.findClassModels(): LinkedList<ClassModel>? {
    if (this.text.equals(VOID)) return null
    val list = LinkedList<ClassModel>()
    // 添加当前
    val psiClass = this.resolvePsiClass() ?: return null
    psiClass.toClassModel()?.let { list.add(it) } ?: return null
    // 数组作为list，特殊处理 && 数组类型不存在泛型
    if (this.text.endsWith(ARRAY_TAG)) {
        list.addFirst(psiClass.scannerService().buildSourceModel(null, FieldType.LIST))
        return list
    }
    // 泛型处理
    PsiTreeUtil.findChildrenOfType(this, PsiTypeElement::class.java)
        // 忽略Map的key部分，只取value
        .filter { !(it?.nextSibling?.text?.contains(",") ?: false) }.map { it.resolvePsiClass()?.toClassModel() }
        .forEach { it?.let { list.add(it) } }
    return if (list.size > 0) list else null
}

@JvmName("toRefClassModel")
fun PsiTypeElement.toRefClassModel(): RefClassModel? {
    // ResultData<List<String>>
    // 1.String -> source, ref = tmp(String)
    // 2.List -> source, ref = tmp(List<String>)
    // 3.ResultData -> source, ref = tmp(ResultData<List<String>>)
    var root: RefClassModel? = null
    val list = this.findClassModels() ?: return null
    do RefClassModel(list.pollLast(), root).apply { root = this } while (list.size > 0)
    return root
}

@JvmName("buildField")
fun PsiParameter.buildField(remark: String): FieldModel? {
    return this.typeElement?.toRefClassModel()?.let { FieldModel(name, remark, it) }
}

/**
 * 通过完整限定名来获取class，支持泛型。但是所有的获取都依赖于cache
 *
 * <ul>
 *     <li>java.util.List<java.util.Map<java.lang.String, java.lang.Integer>></li>
 *     <li>int</li>
 *     <li>java.lang.String</li>
 * </ul>
 */
@JvmName("analyzeRefClassModel")
fun PsiType?.analyzeRefClassModel(project: Project): RefClassModel? {
    var fullClassName = this?.internalCanonicalText?.replace(Regex("[\\w.]+,|\\s+"), "") ?: return null
    if (fullClassName.endsWith(ARRAY_TAG)) {
        // int[] -> java.util.List<int>
        // java.lang.Long[] -> java.util.List<java.lang.Long>
        fullClassName = "${FieldType.LIST.qName}<${fullClassName.removeSuffix(ARRAY_TAG)}>"
    }
    var root: RefClassModel? = null
    var parent: RefClassModel? = null
    // 过滤Map的key限定名 -> java.util.List<java.util.Map<java.lang.Integer>>
    val service = project.scannerService()
    Regex("([\\w.]+)").findAll(fullClassName).map { it.groupValues[1] }.map { service.findClassModel(it) }
        .forEachIndexed { index, classModel ->
            classModel?.toRefClassModel()?.also { if (index == 0) root = it else parent?.ref = it }
                ?.also { parent = it } ?: return root
        }
    return root
}

@JvmName("resolvePsiClass")
fun PsiTypeElement.resolvePsiClass(): PsiClass? {
    val resolve = this.innermostComponentReferenceElement?.resolve() ?: return null
    return if (resolve is PsiClass) resolve else null
}