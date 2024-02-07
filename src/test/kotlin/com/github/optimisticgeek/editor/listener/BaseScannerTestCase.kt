// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimisticgeek.editor.listener

import com.github.optimisticgeek.spring.constant.FieldType
import com.github.optimisticgeek.spring.constant.FieldType.*
import com.github.optimisticgeek.spring.ext.analyze
import com.github.optimisticgeek.spring.ext.isControllerClass
import com.github.optimisticgeek.spring.model.*
import com.github.optimisticgeek.spring.service.SpringScannerService
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase
import junit.framework.TestCase
import java.io.File

/**
 * ScannerServiceTestCase

 * @author OptimisticGeek
 * @date 2024/1/10
 */
@Suppress("MemberVisibilityCanBePrivate")
@TestDataPath("\$CONTENT_ROOT/src/test/testData")
abstract class BaseScannerTestCase : LightJavaCodeInsightFixtureTestCase() {
    val psiFileFactory: PsiFileFactory get() = PsiFileFactory.getInstance(project)
    val cache: HashMap<String, PsiClass> = HashMap()
    protected val service: SpringScannerService get() = project.service()
    val qNameResultData = "com.github.optimisticgeek.entity.ResultData"
    val qNamePager = "com.github.optimisticgeek.entity.Pager"
    val qNameProjectQuery = "com.github.optimisticgeek.query.ProjectQuery"

    override fun setUp() {
        super.setUp()
        values().forEach { ClassModel(it).mockClass() }
        ClassModel(MAP).also { it.position = CommonClassNames.JAVA_UTIL_HASH_MAP }.mockClass()
        ClassModel(LIST).also { it.position = CommonClassNames.JAVA_UTIL_ARRAY_LIST }.mockClass()
        initConsumerModel()
        initModelPath()
    }

    fun findClassModel(qName: String): ClassModel? {
        return service.findClassModel(qName)
    }

    fun initConsumerModel() {
        // Pager
        ClassModel(
            qNamePager, "分页", arrayListOf(
                FieldModel("total", "总条数", INTEGER.mockRefClass()),
                FieldModel("page", "当前页", INTEGER.mockRefClass()),
                FieldModel("rows", "数据集", LIST.mockRefClass())
            )
        ).mockClass()

        ClassModel(
            qNameProjectQuery, "项目查询", arrayListOf(
                FieldModel("name", "project name", INTEGER.mockRefClass()),
                FieldModel("price", "金额", DOUBLE.mockRefClass()),
                FieldModel("keyword", "关键字", LIST.mockRefClass())
            )
        ).mockClass()
    }

    fun buildSourceModel(qName: String?, type: FieldType): ClassModel {
        val key = qName ?: type.qName
        return service.cacheMap.getOrPut(key) { ClassModel(qName = qName ?: type.qName, type = type) }
    }

    fun getCurrentMethodName(): String {
        return Thread.currentThread().stackTrace[2].methodName
    }

    override fun getTestDataPath() = "src/test/testData/scanner"

    fun ClassModel.mockClass(): String {
        assertNotNull(this.isNull())
        if (this.qName.isBlank()) this.position = type.qName

        this.name = this.position!!.className()

        return cache.getOrPut(this.position!!) {
            val sb = StringBuilder()
            sb.appendLine("package ${this.position!!.packageName()};").appendImportList(this.fields)
            sb.appendLine("public class ${this.position!!.className()}${if (this.hasRefField()) "<T>" else ""} {")

            sb.appendFields(this.fields)

            sb.appendLine("}")
            sb.toString().toPsiClass()
        }.qualifiedName!!
    }

    private fun StringBuilder.appendImportList(ref: RefClassModel?) {
        ref ?: return
        ref.source.mockClass().let { qName ->
            if (qName.length == 1) return
            val import = "import $qName;"
            if (!this.contains(import)) {
                this.appendLine(import)
            }
            this.appendImportList(ref.source.fields)
        }
        this.appendImportList(ref.ref)
    }

    private fun StringBuilder.appendImportList(fields: ArrayList<FieldModel>?): StringBuilder {
        fields ?: return this
        fields.forEach { this.appendImportList(it.classType) }
        return this
    }

    private fun initModelPath() {
        File(getModelPath()).listFiles { file -> file.extension == "java" }?.forEach { file ->
            try {
                file.readText().toPsiClass()
            } catch (_: Error) {
                return
            }
        }
    }

    fun String.toPsiClass(): PsiClass {
        return myFixture.addClass(this)
            .also { cache[it.qualifiedName!!] = it }
            .also { if (it.isControllerClass().not()) service.parseClassModel(it) }
    }

    fun PsiFile.toPsiClass(): PsiClass? {
        if (this is PsiJavaFile) this.classes[0]
        return this.let { PsiTreeUtil.findChildOfType(this, PsiClass::class.java) }
    }

    private fun getModelPath(): String = "$testDataPath/model"


    fun RefClassModel.mockRefClass(vararg children: FieldType): RefClassModel {
        var current: RefClassModel = this
        children.forEach { it.mockRefClass().also { current.ref = it }.also { current = it } }
        return this
    }

    fun RefClassModel.mockRefClass(vararg children: RefClassModel): RefClassModel {
        var current: RefClassModel = this
        children.forEach { it.also { current.ref = it }.also { current = it } }
        return this
    }

    fun String.mockRefClass(): RefClassModel {
        return service.cacheMap[this]?.toRefClassModel() ?: service.findClassModel(this)?.toRefClassModel()
        ?: throw RuntimeException("class $this not found")
    }

    fun String.mockRefClass(vararg children: FieldType): RefClassModel {
        return this.mockRefClass().mockRefClass(*children)
    }

    fun FieldType.mockRefClass(): RefClassModel {
        assert(this != OBJECT && this != OTHER)
        return this.qName.mockRefClass()
    }

    fun FieldType.mockClass(): ClassModel {
        return service.cacheMap[this.qName]!!
    }

    fun RefClassModel.assertFullName(qName: String, vararg children: FieldType) {
        this.fullClassName().assertFullName(qName.mockRefClass(*children).fullClassName())
        thisLogger().warn("\n${this.analyze().toJson()}")
    }

    fun String.assertFullName(targetFullName: String) {
        assertTrue("【response != target】 $this != $targetFullName", targetFullName.equals(this))
    }

    fun RefClassModel.assertFullName(targetFullName: String) {
        this.fullClassName().assertFullName(targetFullName)
        thisLogger().warn("\n${this.analyze().toJson()}")
    }

    fun findPsiClass(qName: String): PsiClass {
        return cache[qName] ?: myFixture.findClass(qName)
    }
}

private fun StringBuilder.appendFields(fields: ArrayList<FieldModel>?) {
    fields ?: return
    fields.forEach {
        TestCase.assertNotNull(it.classType)
        this.appendLine("    private ${it.classType.fullClassName()} ${it.name};")
    }
}
