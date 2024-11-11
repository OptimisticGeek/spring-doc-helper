// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimistic.editor.listener

import com.github.optimistic.spring.service.getHttpMethodMap
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.psi.PsiMethod
import java.io.File

/**
 * SpringScannerServiceTest

 * @author OptimisticGeek
 * @date 2024/1/11
 */
open class BaseControllerTestCase : BaseScannerTestCase() {
    open val qNameController = "com.github.optimistic.controller.BaseController"
    open val methods: Map<String, PsiMethod>
        get() = controllerPsiClass.methods.withIndex().associate { it.value.name to it.value }

    open val controllerPsiClass
        get() = findPsiClass(qNameController)
    override fun setUp() {
        super.setUp()
        initControllerPath()
    }

    fun testCreateTmpDocumentFile() {
        // writeTmpDocument()
    }

    fun writeTmpDocument() {
        val tmpPath = "$testDataPath/tmp/document/controller/${this.name}"
        File(tmpPath).mkdirs()
        controllerPsiClass.getHttpMethodMap()?.values?.forEach {
            val analyze = it.analyze()
            File("$tmpPath/${it.name}.html").writeBytes(
                analyze.toHtmlDocument().toByteArray()
            )
        }
    }

    fun getCurrentMethod(): PsiMethod {
        return methods[Thread.currentThread().stackTrace[2].methodName]!!
    }

    private fun getControllerPath(): String = "$testDataPath/controller"

    private fun initControllerPath() {
        val importListRegex = Regex("import\\s+([\\w.]+)\\s*;")
        val annotationRegex = Regex("@([\\w.]+)")
        File(getControllerPath()).listFiles { file -> file.extension == "java" }?.forEach { file ->
            var javaSource = file.readText()
            // 注解需要改为完整限定名字
            annotationRegex.findAll(javaSource).map { it.groupValues[1] }.filter { !it.contains('.') }.distinct()
                .forEach {
                    // 将注解类名转换为完整限定名称，不然注解不生效
                    // 本方法只针对Spring mvc注解进行处理
                    if (it.endsWith("Mapping") || it.contains("Controller") || it.startsWith("Request")) {
                        val replacement = "org.springframework.web.bind.annotation.$it"
                        this.thisLogger().debug(replacement)
                        javaSource = javaSource.replace(Regex(it), replacement)
                    }
                }

            try {
                javaSource.toPsiClass().let { cache[it.qualifiedName!!] = it }
            } catch (_: Error) {
                return
            }
            // 检测依赖
            importListRegex.findAll(javaSource).map { it.groupValues[1] }.forEach {
                try {
                    findPsiClass(it)
                } catch (e: Error) {
                    e.printStackTrace()
                }
            }
        }
    }
}
