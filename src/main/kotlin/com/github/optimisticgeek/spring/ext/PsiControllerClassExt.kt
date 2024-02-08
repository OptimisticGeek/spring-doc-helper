// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimisticgeek.spring.ext

import com.github.optimisticgeek.spring.constant.*
import com.github.optimisticgeek.spring.model.*
import com.intellij.openapi.util.Key
import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiMethodImpl
import com.intellij.psi.impl.source.tree.java.PsiAssignmentExpressionImpl
import com.intellij.psi.impl.source.tree.java.PsiMethodCallExpressionImpl
import com.intellij.psi.impl.source.tree.java.PsiNewExpressionImpl
import com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentOfType
import org.apache.commons.collections.CollectionUtils
import org.apache.commons.lang3.BooleanUtils
import java.util.*

/**
 * build requestBody requestParams pathVariables
 */
fun PsiMethod.buildParameters(methodModel: MethodModel) {
    if (!this.hasParameters()) return
    this.parameterList.parameters.forEach {
        val fieldModel = it.buildField(this.getDocumentTagParam(it.name)) ?: return@forEach
        // requestBody
        if (it.hasAnnotation(REQUEST_BODY)) {
            methodModel.requestBody = fieldModel.also { it.name = null }.also { it.aliasName = null }
            return@forEach
        }
        // pathVariables
        if (it.hasAnnotation(PATH_VARIABLE)) {
            fieldModel.aliasName = it.getAnnotationValue(PATH_VARIABLE, DEFAULT)
            methodModel.pathVariables.add(fieldModel)
            return@forEach
        }
        // requestParams
        if (it.hasAnnotation(REQUEST_PARAM)) {
            fieldModel.aliasName = it.getAnnotationValue(REQUEST_PARAM, DEFAULT)
            fieldModel.isRequired = BooleanUtils.toBoolean(it.getAnnotationValue(REQUEST_PARAM, REQUIRED))
        }
        methodModel.queryParams.add(fieldModel)
    }
}


/**
 * 首先基于typeElement分析，之后尝试对method代码块中的return进行分析
 */
fun PsiMethod.buildResponseBody(): RefClassModel? {
    val responseBody =
        this.returnTypeElement?.toRefClassModel()?.also { it.remark = this.getDocumentTag(RETURN) } ?: return null
    if (responseBody.isBase()) return responseBody

    // List<Long> 说明分析成功，List<>是分析失败的
    responseBody.fullClassName().let { if (it.contains("<") && !it.contains("<>")) return responseBody }
    this.analyzeResponseBody(responseBody)
    return responseBody
}

fun PsiVariable.toRefClassModel(): RefClassModel? {
    return this.typeElement?.toRefClassModel()?.apply { this.ref = buildRefClassModel(this) }
}

private fun PsiVariable.buildRefClassModel(root: RefClassModel): RefClassModel? {
    root.takeIf { !it.isBase() } ?: return null
    val variableName = nameIdentifier?.text
    val fieldName = root.refField?.fieldName
    PsiTreeUtil.findChildrenOfType(this.parentOfType<PsiCodeBlock>(), PsiIdentifier::class.java)
        .filter { it.text == variableName }
        // 验证变量名，例如 result.setData(1) || result.data=1
        .filter {
            !(root.sourceType == FieldType.OBJECT && !it.parent.parent.text.matches(
                Regex("^$variableName.(set)?$fieldName$", RegexOption.IGNORE_CASE)
            ))
        }.forEach {
            when (val parent = it.parent.parent.parent) {
                // PsiMethodCallExpression:result.setData(new ArrayList<Integer>()) || result.setData(1)
                is PsiMethodCallExpressionImpl -> parent.analyzeResponseBody(root, null)
                // PsiAssignmentExpression:result.data = new ArrayList<Integer>() || result.data = 1
                is PsiAssignmentExpressionImpl -> PsiTreeUtil.getChildOfAnyType(
                    parent, PsiLiteralExpression::class.java, PsiNewExpressionImpl::class.java
                )?.let { it.type.analyzeRefClassModel(project) }

                else -> null
            }.let { root.updateRef(it) }
        }
    return root.ref
}

private fun PsiMethod.analyzeResponseBody(root: RefClassModel): RefClassModel? {
    // 泛型或者Object的字段
    val refField = root.refField
    val returnStatements = PsiTreeUtil.findChildrenOfAnyType(this.body, true, PsiReturnStatement::class.java)
    returnStatements.asSequence().flatMap {
        PsiTreeUtil.findChildrenOfAnyType(
            it,
            false,
            PsiNewExpressionImpl::class.java,
            PsiMethodCallExpression::class.java,
            PsiReferenceExpressionImpl::class.java
        )
    }.distinctBy { it.text }.mapNotNull {
        if (!(it.parent is PsiReturnStatement || it.parent.parent is PsiReturnStatement)) return@mapNotNull null
        try {
            return@mapNotNull when (it) {
                is PsiNewExpressionImpl -> it.analyzeResponseBody(refField?.fieldName)
                is PsiMethodCallExpressionImpl -> it.analyzeResponseBody(root, refField?.fieldName)
                is PsiReferenceExpressionImpl -> it.analyzeResponseBody(root, refField?.fieldName)
                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
    }.forEach { root.updateRef(it) }
    return root.ref
}

/**
 * 例子：return result
 */
private fun PsiReferenceExpressionImpl.analyzeResponseBody(root: RefClassModel, fieldName: String?): RefClassModel? {
    if (!(this.parent is PsiReturnStatement || this.parent.parent is PsiReturnStatement)) return null
    return when (val element = resolve()) {
        is PsiVariable -> element.buildRefClassModel(root)
        is PsiMethodImpl -> element.parentOfType<PsiMethodCallExpression>()?.analyzeResponseBody(root, fieldName)
        else -> null
    }
}

fun PsiMethodCallExpression.analyzeResponseBody(): RefClassModel? {
    return this.resolveMethod()?.returnTypeElement?.toRefClassModel()?.also { root ->
        this.analyzeResponseBody(root, root.refField?.name).let { root.updateRef(it) }
    }
}

/**
 * 例子：return ResultData.success(); 【忽略】
 * 例子：return ResultData.success(data); 】【收集该data值，放到ref】
 */
private fun PsiMethodCallExpression.analyzeResponseBody(root: RefClassModel, fieldName: String?): RefClassModel? {
    // 分析返回值，存在三种情况
    // 1.source类型一致，当前评分高于原有评分
    this.resolveMethod()?.returnTypeElement?.toRefClassModel()?.also { root.updateRef(it.ref) }

    // 2.source类型不一致，并且不为空
    // if(responseBody != null && responseBody.source != source.source) return responseBody

    // 3.分析方法参数，定位入参真实类型
    return this.analyzeResponseBody(fieldName).also { root.updateRef(it) }
}

/**
 * 例子：return new ResultData(); 【忽略】
 * 例子：return new ResultData(data); 】【收集该data值，放到ref】
 */
private fun PsiCallExpression.analyzeResponseBody(fieldName: String?): RefClassModel? {
    val params = this.argumentList?.expressions
    if (params.isNullOrEmpty()) return null
    val fieldIndex = if (params.size == 1 || fieldName.isNullOrBlank()) 0 else {
        // 查找字段对应索引
        val psiMethod = if (this is PsiNewExpression) this.resolveConstructor() else this.resolveMethod()
        psiMethod?.parameterList?.parameters?.mapIndexed { index, psiParameter -> if (psiParameter.name == fieldName) index else null }
            ?.firstOrNull()
    } ?: return null
    // 查找字段在方法中的顺序，用以确定入参类型
    return params[fieldIndex]?.type.analyzeRefClassModel(project)
}

private val CACHE_KEY_CONTROLLER: Key<ControllerModel> = Key.create("springDocHelper.controller.cache")
fun PsiClass.clearControllerCache() {
    this.putUserData(CACHE_KEY_CONTROLLER, null)
}

fun PsiClass.createControllerModel(): ControllerModel? {
    if (!this.isControllerClass()) return null

    val controller = this.getUserData(CACHE_KEY_CONTROLLER)
        ?: ControllerModel(this).also { if (CollectionUtils.isEmpty(it.urls)) return null }

    this.methods.filter { it.hasModifierProperty(PsiModifier.PUBLIC) && it.getHttpRequestAnnotation() != null }
        .mapNotNull { it.buildMethodModel(controller) }.associateBy { it.psiMethod }.let { controller.methodMap = it }

    if (controller.methodMap.isNullOrEmpty()) return null
    return controller.also { this.putUserData(CACHE_KEY_CONTROLLER, controller) }
}

fun PsiMethod.buildMethodModel(controller: ControllerModel): MethodModel {
    return MethodModel(this).apply {
        if (this.author.isNullOrBlank()) this.author = controller.author
        urls = joinerUrl(controller.urls, psiMethodAnnotation.getAnnotationValues(DEFAULT))
        isViewer = BooleanUtils.isTrue(controller.isViewer) && !this@buildMethodModel.hasAnnotation(RESPONSE_BODY)
        responseBody = this@buildMethodModel.buildResponseBody()
        this@buildMethodModel.buildParameters(this)
    }
}

private fun joinerUrl(controllerUrls: List<String>?, methodUrls: List<String>?): List<String>? {
    if (methodUrls.isNullOrEmpty()) {
        return Collections.emptyList()
    }
    if (controllerUrls.isNullOrEmpty()) {
        return methodUrls
    }
    return controllerUrls.flatMap { controller ->
        methodUrls.map { "${controller.removeSuffix("/")}/${it.removePrefix("/").removeSuffix("/")}" }
    }
}

fun PsiMethod.getHttpRequestAnnotation(): PsiAnnotation? {
    return this.getAnnotation(GET_MAPPING) ?: this.getAnnotation(POST_MAPPING) ?: this.getAnnotation(PUT_MAPPING)
    ?: this.getAnnotation(DELETE_MAPPING) ?: this.getAnnotation(REQUEST_MAPPING)
}

fun PsiClass.isControllerClass(): Boolean {
    if (this.methods.isEmpty()) {
        return false
    }
    return this.isValid && this.isWritable && this.hasAnnotation(REQUEST_MAPPING)
}