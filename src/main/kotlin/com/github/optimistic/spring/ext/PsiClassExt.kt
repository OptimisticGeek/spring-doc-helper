// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimistic.spring.ext

import com.github.optimistic.spring.constant.nonSerializedAnnotations
import com.github.optimistic.spring.constant.optionalAnnotations
import com.github.optimistic.spring.model.type.BaseClass
import com.github.optimistic.spring.model.type.ObjClass
import com.github.optimistic.spring.parse.parseService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiModifierListOwner
import com.intellij.psi.PsiType
import com.intellij.psi.PsiTypeElement

/**
 * 检测是否是必填字段
 * @return null:不进行解析，true:是必填字段，false:不是必填字段，默认为必填
 */
@JvmName("checkPsiFieldRequired")
fun PsiModifierListOwner.checkRequired(): Boolean? {
    // 表示不序列化的注解列表
    if (this.checkAnnotations(nonSerializedAnnotations, false)) return null
    // 表示字段可选的注解列表
    if (this.checkAnnotations(optionalAnnotations, false)) return false
    // 表示字段必填的注解列表  默认为true，则必定为true
    // if (this.checkAnnotations(requiredAnnotations, true)) return true
    // 默认值
    return true
}

/**
 * 检测是否包含指定的注解
 */
@JvmName("checkAnnotations")
private fun PsiModifierListOwner.checkAnnotations(qNames: Set<String>, default: Boolean = true): Boolean {
    // qName:param:value
    qNames.map { it.split(":") }.forEach { split ->
        getAnnotation(split[0])?.let {
            return split.size < 3 || split[2].lowercase() == it.getAnnotationValue(split[1]).lowercase()
        }
    }
    return default
}

@JvmName("getBaseClass1")
fun PsiTypeElement?.getBaseClass(): BaseClass? = this?.parseService()?.getBaseClass(this.type)

@JvmName("getBaseClass")
fun PsiType?.getBaseClass(): BaseClass? = this?.parseService()?.getBaseClass(this)

@JvmField
val qNameRegex = Regex("([\\w.]+(\\[])*),?")

/**
 * 通过qName获取类，支持java.util.Map<java.lang.String,java.utils.List<java.lang.Object>>
 */
fun Project.getBaseClass(qName: String?): BaseClass? = this.parseService().getBaseClass(qName = qName)

fun String.packageName(): String {
    if (!this.contains(".")) return this
    return this.substring(0, this.lastIndexOf("."))
}

@JvmName("className")
fun String.className(): String {
    if (!this.contains(".")) return this
    return this.substring(this.lastIndexOf(".") + 1).replace("<", "").replace(">", ".")
}

@JvmName("resolvePsiClass")
fun PsiTypeElement.resolvePsiClass(): PsiClass? = this.innermostComponentReferenceElement?.resolve() as? PsiClass

@JvmName("trimQname")
fun String.firstQualifiedName(): String {
    if (this == "java.lang.Class<?>") return "?"
    return this.replaceFirst(Regex("<.*?$"), "")
}

/**
 * 刷新类的字段
 *
 * @param isForce 是否强制刷新，强制刷新会把fields清空重新加载
 */
@JvmName("refreshObjClass")
fun <T : ObjClass> T.refreshFields(psi: PsiClass?, isForce: Boolean = false): T {
    psi ?: return this
    if (isForce) this.isInit = false
    psi.project.parseService().refreshFields(psi, this, isForce)
    return this
}