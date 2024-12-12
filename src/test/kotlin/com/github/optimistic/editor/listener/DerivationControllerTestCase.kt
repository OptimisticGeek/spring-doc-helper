package com.github.optimistic.editor.listener

import com.github.optimistic.spring.constant.FieldType

/**
 * SpringScannerServiceTest

 * @author OptimisticGeek
 * @date 2024/1/11
 */
class DerivationControllerTestCase : BaseControllerTestCase() {
    override val qNameController = "com.github.optimistic.controller.DerivationController"
    private val qNameAjaxResult = "com.github.optimistic.entity.AjaxResult"

    fun test1() {
        val psiMethod = getCurrentMethod()
        psiMethod.parseBaseClass().assertFullName(qNameAjaxResult, FieldType.INTEGER)
    }

    fun test2() {
        val psiMethod = getCurrentMethod()
        psiMethod.parseBaseClass().assertFullName(qNameAjaxResult, FieldType.LIST, FieldType.INTEGER)
    }

    fun test3() {
        val psiMethod = getCurrentMethod()
        psiMethod.parseBaseClass().assertFullName(qNameAjaxResult, FieldType.INTEGER)
    }

    fun test4() {
        val psiMethod = getCurrentMethod()
        psiMethod.parseBaseClass().assertFullName(qNameAjaxResult, FieldType.INTEGER)
    }

    fun test5() {
        val psiMethod = getCurrentMethod()
        psiMethod.parseBaseClass().assertFullName(qNameAjaxResult, FieldType.INTEGER)
    }

    fun test6() {
        val psiMethod = getCurrentMethod()
        psiMethod.parseBaseClass().assertFullName(qNameAjaxResult, FieldType.INTEGER)
    }

    fun test7() {
        val psiMethod = getCurrentMethod()
        psiMethod.parseBaseClass().assertFullName(qNameResultData, FieldType.INTEGER)
    }


    fun test8() {
        val psiMethod = getCurrentMethod()
        psiMethod.parseBaseClass().assertFullName(qNameResultData, FieldType.LIST, FieldType.INTEGER)
    }

    fun test9() {
        val psiMethod = getCurrentMethod()

        val mockRefClass = qNameResultData.mockBaseClass().mockBaseClass(
            FieldType.LIST.mockBaseClass(), FieldType.INTEGER.mockBaseClass()
        )

        psiMethod.parseBaseClass().assertFullName(mockRefClass)
    }

    fun test10() {
        val psiMethod = getCurrentMethod()

        val mockRefClass = qNameResultData.mockBaseClass().mockBaseClass(
            FieldType.LIST.mockBaseClass(), qNameResultData.mockBaseClass(), FieldType.INTEGER.mockBaseClass()
        )

        psiMethod.parseBaseClass().assertFullName(mockRefClass)
    }

    fun test11() {
        val psiMethod = getCurrentMethod()
        val mockRefClass = qNameResultData.mockBaseClass().mockBaseClass(
            FieldType.LIST.mockBaseClass(), FieldType.INTEGER.mockBaseClass()
        )
        psiMethod.parseBaseClass().assertFullName(mockRefClass)
    }

    fun test12() {
        // ResultData<Map<String,List<Integer>>>
        val psiMethod = getCurrentMethod()
        val mockRefClass = qNameResultData.mockBaseClass().mockBaseClass(
            FieldType.MAP.mockBaseClass(), FieldType.LIST.mockBaseClass(), FieldType.INTEGER.mockBaseClass()
        )
        psiMethod.parseBaseClass().assertFullName(mockRefClass)
    }

    fun test13() {
        // ResultData<Map<String,ResultData<List<Integer>>>>
        val psiMethod = getCurrentMethod()
        val mockRefClass = qNameResultData.mockBaseClass().mockBaseClass(
            FieldType.MAP.mockBaseClass(),
            qNameResultData.mockBaseClass(),
            FieldType.LIST.mockBaseClass(),
            FieldType.INTEGER.mockBaseClass()
        )
        psiMethod.parseBaseClass().assertFullName(mockRefClass)
    }

    fun test14() {
        // ResultData<Map<String,ResultData<List<Integer>>>>
        val psiMethod = getCurrentMethod()
        val mockRefClass = qNameResultData.mockBaseClass().mockBaseClass(
            FieldType.LIST.mockBaseClass(),
            FieldType.INTEGER.mockBaseClass()
        )
        psiMethod.parseBaseClass().assertFullName(mockRefClass)
    }
}
