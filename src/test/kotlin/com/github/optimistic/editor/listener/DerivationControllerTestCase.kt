// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimistic.editor.listener

import com.github.optimistic.spring.constant.FieldType.INTEGER
import com.github.optimistic.spring.constant.FieldType.LIST
import com.github.optimistic.spring.ext.buildResponseBody
import com.github.optimistic.spring.model.fullClassName

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
        psiMethod.buildResponseBody()!!.assertFullName(qNameAjaxResult, INTEGER)
    }

    fun test2() {
        val psiMethod = getCurrentMethod()
        psiMethod.buildResponseBody()!!.assertFullName(qNameAjaxResult, LIST, INTEGER)
    }

    fun test3() {
        val psiMethod = getCurrentMethod()
        psiMethod.buildResponseBody()!!.assertFullName(qNameAjaxResult, INTEGER)
    }

    fun test4() {
        val psiMethod = getCurrentMethod()
        psiMethod.buildResponseBody()!!.assertFullName(qNameAjaxResult, INTEGER)
    }

    fun test5() {
        val psiMethod = getCurrentMethod()
        psiMethod.buildResponseBody()!!.assertFullName(qNameAjaxResult, INTEGER)
    }

    fun test6() {
        val psiMethod = getCurrentMethod()
        psiMethod.buildResponseBody()!!.assertFullName(qNameAjaxResult, INTEGER)
    }

    fun test7() {
        val psiMethod = getCurrentMethod()
        psiMethod.buildResponseBody()!!.assertFullName(qNameResultData, INTEGER)
    }


    fun test8() {
        val psiMethod = getCurrentMethod()
        psiMethod.buildResponseBody()!!.assertFullName(qNameResultData, LIST, INTEGER)
    }

    fun test9() {
        val psiMethod = getCurrentMethod()

        val mockRefClass = qNameResultData.mockRefClass().mockRefClass(
            LIST.mockRefClass(), qNameResultData.mockRefClass(), INTEGER.mockRefClass()
        )

        psiMethod.buildResponseBody()!!.assertFullName(mockRefClass.fullClassName())
    }

    fun test10() {
        val psiMethod = getCurrentMethod()

        val mockRefClass = qNameResultData.mockRefClass().mockRefClass(
            LIST.mockRefClass(), qNameResultData.mockRefClass(), INTEGER.mockRefClass()
        )

        psiMethod.buildResponseBody()!!.assertFullName(mockRefClass.fullClassName())
    }
}
