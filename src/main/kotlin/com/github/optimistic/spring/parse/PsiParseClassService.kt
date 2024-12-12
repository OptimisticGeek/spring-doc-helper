package com.github.optimistic.spring.parse

import com.github.optimistic.spring.model.ClassType
import com.github.optimistic.spring.model.type.BaseClass
import com.github.optimistic.spring.model.type.ObjClass
import com.github.optimistic.spring.model.type.StaticValueClass
import com.intellij.psi.*

interface PsiParseClassService {

    /**
     * 分析类的字段，通过构造函数、复制、getter进行分析
     */
    @PsiParseLead("psi.parse.refreshFields", LeadPolicy.PARENT)
    fun refreshFields(psi: PsiClass, obj: ObjClass, isForce: Boolean? = false): ClassType

    @PsiParseLead("psi.parse.getBaseClass", LeadPolicy.PARENT)
    fun getBaseClass(psi: PsiTypeElement?): BaseClass?

    @PsiParseLead("psi.parse.getBaseClass", LeadPolicy.PARENT)
    fun getBaseClass(psi: PsiType?): BaseClass?

    /**
     * 支持qName全路径解析
     * 例如：ResultData<List<Integer>>
     */
    @PsiParseLead("psi.parse.getBaseClass", LeadPolicy.PARENT)
    fun getBaseClass(qName: String? = null, psi: PsiClass? = null): BaseClass?

    /**
     * 任意element识别
     */
    @PsiParseLead("psi.parse.parseBaseClass", LeadPolicy.PARENT)
    fun parseBaseClass(psi: PsiElement, from: PsiElement? = psi): BaseClass?

    /**
     * 解析方法形参、实参以及codeBlock
     */
    @PsiParseLead("psi.parse.parseMethodBaseClass", LeadPolicy.PARENT)
    fun parseMethodBaseClass(psi: PsiMethod, call: PsiCallExpression? = null): BaseClass?

    @PsiParseLead("psi.parse.parseReturnBaseClass", LeadPolicy.PARENT)
    fun parseReturnBaseClass(psi: PsiMethod, call: PsiCallExpression? = null): BaseClass?

    /**
     * 解析return代码行
     */
    @PsiParseLead("psi.parse.parseReturnBaseClass", LeadPolicy.PARENT)
    fun parseReturnBaseClass(psi: PsiReturnStatement): BaseClass?

    /**
     * 解析方法参数
     */
    @PsiParseLead("psi.parse.parseParamsBaseClass", LeadPolicy.PARENT)
    fun parseParamsBaseClass(psi: PsiNewExpression, call: PsiCallExpression? = psi): BaseClass?

    /**
     * 解析methodCall
     */
    @PsiParseLead("psi.parse.parseMethodCallBaseClass", LeadPolicy.PARENT)
    fun parseMethodCallBaseClass(psi: PsiMethodCallExpression, root: ClassType? = null): BaseClass?

    @PsiParseLead("psi.parse.parseParamsBaseClass", LeadPolicy.PARENT)
    fun parseParamsBaseClass(psi: PsiMethod, root: ClassType, call: PsiCallExpression? = null): ClassType?

    /**
     * 添加线索，并且会尝试匹配field
     */
    @PsiParseLead("psi.parse.putLead", LeadPolicy.PARENT)
    fun putLead(psi: PsiMethod, call: PsiCallExpression? = null, root: ClassType)

    /**
     * 解析getter方法
     */
    @PsiParseLead("psi.parse.parseBaseClassAtGetter", LeadPolicy.PARENT)
    fun parseBaseClassAtGetter(psi: PsiMethod): Pair<String, BaseClass>?

    /**
     * 解析setter方法
     */
    @PsiParseLead("psi.parse.parseBaseClassAtSetter", LeadPolicy.PARENT)
    fun parseBaseClassAtSetter(psi: PsiMethod, call: PsiCallExpression?, root: ClassType): Boolean?

    /**
     * 解析map.put
     */
    @PsiParseLead("psi.parse.parseBaseClassAtMapPut", LeadPolicy.PARENT)
    fun parseBaseClassAtMapPut(psi: PsiMethod, root: ClassType, call: PsiCallExpression): Boolean?

    /**
     * 解析常量文本
     */
    @PsiParseLead("psi.parse.parseLiteralBaseClass", LeadPolicy.PARENT)
    fun parseLiteralBaseClass(psi: PsiLiteralExpression): StaticValueClass?

    /**
     * 解析赋值 this.data = data
     */
    @PsiParseLead("psi.parse.parseAssignmentBaseClass", LeadPolicy.PARENT)
    fun parseAssignmentBaseClass(psi: PsiAssignmentExpression, root: ClassType): ClassType?

    /**
     * 解析引用类
     */
    @PsiParseLead("psi.parse.parseJavaCodeRefBaseClass", LeadPolicy.PARENT)
    fun parseJavaCodeRefBaseClass(psi: PsiJavaCodeReferenceElement): BaseClass?

    @PsiParseLead("psi.parse.parseConditionalBaseClass", LeadPolicy.PARENT)
    fun parseConditionalBaseClass(psi: PsiConditionalExpression): BaseClass?

    /**
     * 解析常量值
     */
    @PsiParseLead("psi.parse.getConstantValue", LeadPolicy.PARENT)
    fun getConstantValue(psi: PsiElement): Any?

    @PsiParseLead("psi.parse.parseVariableBaseClass", LeadPolicy.PARENT)
    fun parseVariableBaseClass(psi: PsiVariable): BaseClass?
}