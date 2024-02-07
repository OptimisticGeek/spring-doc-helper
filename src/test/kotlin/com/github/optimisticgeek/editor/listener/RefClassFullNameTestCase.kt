// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimisticgeek.editor.listener

import com.github.optimisticgeek.spring.constant.FieldType.*
import com.github.optimisticgeek.spring.ext.analyze
import com.github.optimisticgeek.spring.model.ClassModel
import com.github.optimisticgeek.spring.model.FieldModel
import com.github.optimisticgeek.spring.model.RefClassModel
import com.intellij.testFramework.TestDataPath
import java.io.File

/**
 * SpringApiDocumentProviderKtTest
 *
 * @author OptimisticGeek
 * @date 2024/1/7
 */
@TestDataPath("\$CONTENT_ROOT/src/test/testData")
class RefClassFullNameTestCase : BaseScannerTestCase() {

    fun testFullClassName1() {

        val source = ClassModel(OBJECT).apply {
            position = qNameResultData
            fields = arrayListOf(FieldModel("data", "数据", RefClassModel(ClassModel(SUBSTITUTE))))
        }

        val refClassModel = RefClassModel(source).apply {
            ref = RefClassModel(ClassModel(MAP)).apply {
                ref = RefClassModel(ClassModel(LIST), RefClassModel(ClassModel(LONG)))
            }
        }
        refClassModel.assertFullName("ResultData<Map<String, List<Long>>>")
        refClassModel.writeTmpFile()
    }

    fun testFullClassName2() {
        val refClassModel = qNameResultData.mockRefClass().mockRefClass(LIST, LIST, LONG)
        refClassModel.assertFullName("ResultData<List<List<Long>>>")
        refClassModel.writeTmpFile()
    }

    fun testFullClassName3() {
        val refClassModel = qNameResultData.mockRefClass(MAP, LIST, LONG)
        refClassModel.assertFullName("ResultData<Map<String, List<Long>>>")
        refClassModel.writeTmpFile()
    }

    fun testFullClassName4() {
        val refClassModel = qNameResultData.mockRefClass(MAP, MAP, LIST, LONG)
        refClassModel.assertFullName("ResultData<Map<String, Map<String, List<Long>>>>")
        refClassModel.writeTmpFile()
    }

    fun testFullClassName5() {
        val refClassModel = qNameResultData.mockRefClass(MAP, LIST, MAP, MAP, LONG)
        refClassModel.assertFullName("ResultData<Map<String, List<Map<String, Map<String, Long>>>>>")
        refClassModel.writeTmpFile()
    }

    fun testFullClassName6() {
        val refClassModel = qNameResultData.mockRefClass()
        refClassModel.mockRefClass(qNamePager.mockRefClass(INTEGER))
        refClassModel.assertFullName("ResultData<Pager<Integer>>")
        refClassModel.writeTmpFile()
    }

    fun testFullClassName(){
        val refClassModel = qNameResultData.mockRefClass()
        refClassModel.source.fields = arrayListOf(
            FieldModel("current", "自身", LIST.mockRefClass().mockRefClass(refClassModel))
        )
        refClassModel.writeTmpFile()
    }


    private fun RefClassModel?.writeTmpFile() {
        val analyze = this?.analyze() ?: return
        val tmpPath = "$testDataPath/tmp/document/model"
        File(tmpPath).mkdirs()
        File("$tmpPath/test.html").writeBytes(analyze.toHtmlDocument().toByteArray())
    }
}