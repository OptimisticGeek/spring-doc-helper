// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimistic.editor.listener

import com.github.optimistic.analyze.model.analyze
import com.github.optimistic.spring.constant.FieldType
import com.github.optimistic.spring.ext.className
import com.github.optimistic.spring.ext.getBaseClass
import com.github.optimistic.spring.ext.isControllerClass
import com.github.optimistic.spring.ext.packageName
import com.github.optimistic.spring.model.ClassType
import com.github.optimistic.spring.model.Field
import com.github.optimistic.spring.model.type.*
import com.github.optimistic.spring.parse.PsiParseJavaService
import com.github.optimistic.spring.parse.parseService
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.LogLevel
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
    val psiFileFactory: PsiFileFactory by lazy { PsiFileFactory.getInstance(project) }
    val cache: HashMap<String, PsiClass> = HashMap()
    val qNameResultData = "com.github.optimistic.entity.ResultData"
    val qNamePager = "com.github.optimistic.entity.Pager"
    val qNameProjectQuery = "com.github.optimistic.query.ProjectQuery"
    val parseService
        get() = project.service<PsiParseJavaService>()

    fun PsiElement.parseBaseClass(): BaseClass? {
        return parseService.parseBaseClass(this)
    }

    override fun setUp() {
        super.setUp()
        initConsumerModel()
        initModelPath()
        thisLogger().setLevel(LogLevel.ALL)

    }

    fun initConsumerModel() {

    }

    fun getCurrentMethodName(): String {
        return Thread.currentThread().stackTrace[2].methodName
    }

    override fun getTestDataPath() = "src/test/testData/scanner"

    fun ObjClass.mockClass(): String {
        return cache.getOrPut(this.qName) {
            val sb = StringBuilder()
            sb.appendLine("package ${this.qName.packageName()};").appendImportList(this.fields)
            sb.appendLine("public class ${this.qName.className()}${if (this.targetFields != null) "<T>" else ""} {")

            sb.appendFields(this.fields)

            sb.appendLine("}")
            sb.toString().toPsiClass()
        }.qualifiedName!!
    }

    private fun StringBuilder.appendImportList(type: BaseClass?) {
        type ?: return
        type.apply {
            if (qName.length == 1) return
            val import = "import $qName;"
            if (contains(import)) appendLine(import)
        }
        type as? ClassType ?: return
        this.appendImportList(type.target)
        this.appendImportList(type.fields)
    }

    private fun StringBuilder.appendImportList(fields: List<Field>?): StringBuilder {
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
        return myFixture.addClass(this).also { cache[it.qualifiedName!!] = it }
            .also { if (it.isControllerClass().not()) project.getBaseClass(it.qualifiedName) }
    }

    fun PsiFile.toPsiClass(): PsiClass? {
        if (this is PsiJavaFile) this.classes[0]
        return this.let { PsiTreeUtil.findChildOfType(this, PsiClass::class.java) }
    }

    private fun getModelPath(): String = "$testDataPath/model"


    fun BaseClass.mockBaseClass(vararg children: FieldType): BaseRefClass {
        this as BaseRefClass
        var current: BaseClass = this
        children.forEach {
            current as? BaseRefClass ?: return@forEach
            it.mockBaseClass().also { (current as BaseRefClass).target = it }.also { current = it }
        }
        return this
    }

    fun BaseClass.mockBaseClass(vararg children: BaseClass): BaseClass {
        var current: BaseClass = this
        children.forEach {
            current as? BaseRefClass ?: return@forEach
            it.also { (current as BaseRefClass).target = it }.also { current = it }
        }
        return this
    }

    fun String.mockBaseClass(): BaseClass =
        project.parseService().getBaseClass(this) ?: throw RuntimeException("class $this not found")

    fun String.mockRefClass(vararg children: FieldType): BaseRefClass = this.mockBaseClass().mockBaseClass(*children)

    fun FieldType.mockBaseClass(): BaseClass = this.baseClass ?: when (this) {
        FieldType.ARRAY, FieldType.LIST -> ListClass()
        FieldType.SUBSTITUTE -> SubstituteClass()
        FieldType.MAP -> MapClass(qName = FieldType.MAP.qName)
        else -> ObjClass(FieldType.OBJECT.qName)
    }.let { ClassType(it) }

    fun BaseClass?.assertFullName(qName: String, vararg children: FieldType) {
        this.assertFullName(qName.mockRefClass(*children).toString())
    }

    fun String.assertFullName(targetFullName: String) {
        val source = project.parseService().getBaseClass(qName = targetFullName).toString()
        assertTrue("【response != target】 $source != $targetFullName", targetFullName == this)
    }

    fun BaseClass?.assertFullName(target: BaseClass) = assertFullName(target.toString())

    fun BaseClass?.assertFullName(targetFullName: String) {
        assertTrue("【response != target】 $this != $targetFullName", targetFullName == this.toString())
        thisLogger().warn("\n${this!!.analyze().toJson()}")
    }

    fun findPsiClass(qName: String): PsiClass = cache[qName] ?: myFixture.findClass(qName)
}

@JvmName("appendFields")
private fun StringBuilder.appendFields(fields: List<Field>?) {
    fields ?: return
    fields.forEach {
        TestCase.assertNotNull(it.classType)
        this.appendLine("    private ${it.classType.toString()} ${it.name};")
    }
}
