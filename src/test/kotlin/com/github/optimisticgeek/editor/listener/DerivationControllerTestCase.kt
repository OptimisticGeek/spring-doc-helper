// Copyright 2023-2024 79127. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimisticgeek.editor.listener

import com.github.optimisticgeek.spring.constant.FieldType.INTEGER
import com.github.optimisticgeek.spring.constant.FieldType.LIST
import com.github.optimisticgeek.spring.ext.buildResponseBody
import com.github.optimisticgeek.spring.model.fullClassName

/**
 * SpringScannerServiceTest

 * @author OptimisticGeek
 * @date 2024/1/11
 */
class DerivationControllerTestCase : BaseControllerTestCase() {
    override val qNameController = "com.github.optimisticgeek.controller.DerivationController"
    private val qNameAjaxResult = "com.github.optimisticgeek.entity.AjaxResult"

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
