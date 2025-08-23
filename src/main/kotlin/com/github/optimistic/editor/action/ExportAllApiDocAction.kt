package com.github.optimistic.editor.action

import com.github.optimistic.analyze.model.toDocument
import com.github.optimistic.editor.listener.analyze
import com.github.optimistic.editor.listener.copyString
import com.github.optimistic.spring.model.HttpMethodModel
import com.github.optimistic.spring.service.springApiService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.module.ModuleUtil
import java.io.File

/**
 * 导出所有接口文档
 */
class ExportAllApiDocAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        event.project?.springApiService()?.searchMethods(limit = Int.MAX_VALUE)?.saveMarkdownFiles()
    }
}


/**
 * 保存Markdown接口文件
 */
@JvmName("saveMarkdownFiles")
fun List<HttpMethodModel>.saveMarkdownFiles() {
    groupBy { it.psiClass }.forEach { psiClass, list ->
        val className = psiClass.name ?: "UnknownClass"
        val myModule = ModuleUtil.findModuleForFile(psiClass.containingFile)!!
        val file = File("${psiClass.project.basePath}${File.separator}.doc${File.separator}${myModule.name}${File.separator}$className.md").apply { parentFile.mkdirs() }
        file.bufferedWriter().use { writer ->
            list.forEachIndexed { i, httpMethod ->
                writer.write(httpMethod.analyze().toDocument())
            }
        }
    }
}

/**
 * 复制接口的Markdown文档
 */
@JvmName("copyMarkdownStr")
fun List<HttpMethodModel>.copyMarkdownStr() {
    val sb = StringBuilder()
    forEach { it.analyze().toDocument(sb) }
    first().project.copyString(sb.toString())
}