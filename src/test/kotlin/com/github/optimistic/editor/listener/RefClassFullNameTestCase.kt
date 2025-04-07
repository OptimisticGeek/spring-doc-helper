// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimistic.editor.listener

import com.intellij.testFramework.TestDataPath

/**
 * SpringApiDocumentProviderKtTest
 *
 * @author OptimisticGeek
 * @date 2024/1/7
 */
@TestDataPath("\$CONTENT_ROOT/src/test/testData")
class RefClassFullNameTestCase : BaseScannerTestCase() {

    fun testFullClassName1() =
        "ResultData<Map<String, List<Long>>>".mockBaseClass().assertFullName("ResultData<Map<String, List<Long>>>")

    fun testFullClassName2() =
        "ResultData<List<List<Long>>>".mockBaseClass().assertFullName("ResultData<List<List<Long>>>")

    fun testFullClassName3() =
        "ResultData<Map<String, List<Long>>>".mockBaseClass().assertFullName("ResultData<Map<String, List<Long>>>")

    fun testFullClassName4() = "ResultData<Map<String, Map<String, List<Long>>>>".mockBaseClass()
        .assertFullName("ResultData<Map<String, Map<String, List<Long>>>>")

    fun testFullClassName5() = "ResultData<Map<String, List<Map<String, Map<String, Long>>>>>".mockBaseClass()
        .assertFullName("ResultData<Map<String, List<Map<String, Map<String, Long>>>>>")

    fun testFullClassName6() = "ResultData<Pager<Integer>>".mockBaseClass().assertFullName("ResultData<Pager<Integer>>")
}