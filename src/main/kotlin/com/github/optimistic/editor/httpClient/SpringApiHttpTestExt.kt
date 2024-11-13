// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.github.optimistic.editor.httpClient

import com.github.optimistic.analyze.model.AnalyzeHttpMethod
import com.github.optimistic.editor.listener.toJson
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.findDocument
import java.io.File

@JvmName("createHttpTestFile")
fun AnalyzeHttpMethod.createHttpTestFile(isShow: Boolean = true, flushed: Boolean = false) {
    val keyword = "### $position"

    // 创建多环境变量文件
    LocalFileSystem.getInstance().refreshAndFindFileByIoFile(project.createHttpTestEvnFile())

    val virtualFile =
        project.getHttpTestFile(position!!).let { LocalFileSystem.getInstance().refreshAndFindFileByIoFile(it) }
            ?: return
    val document = virtualFile.findDocument() ?: return
    FileDocumentManager.getInstance().saveDocument(document)
    document.run {
        runWriteAction {
            if (text.indexOf(keyword) == -1) {
                setText(text + createHttpTestStr())
            } else if (flushed) {
                setText(text.replace(Regex("(### $position[\\s\\S]*?$END_FIX)"), createHttpTestStr()))
            }
        }
    }
    FileDocumentManager.getInstance().saveDocument(document)

    document.text.indexOf(keyword).let { OpenFileDescriptor(project, virtualFile, it).navigate(isShow) }
}

@JvmName("hasHttpTestFile")
fun AnalyzeHttpMethod.hasHttpTestMethod(): Boolean {
    return project.getHttpTestFile(position!!, false).also { if (!it.exists()) return false }.readText()
        .contains("### $position")
}

private const val END_FIX = "### END\n\n\n\n"

@JvmName("createHttpTestStr")
private fun AnalyzeHttpMethod.createHttpTestStr(): String {
    val sb = StringBuilder("### $position\n")
    sb.appendLine("# @name ${if (remark.isNullOrBlank()) name else remark} $author").appendLine()
    sb.appendLine("$httpMethod {{host}}$url")
    requestBody?.let { sb.appendLine("Content-Type: application/json") }
    response?.let { sb.appendLine("Accept: application/json") }
    sb.appendLine("Authorization: {{token}}").appendLine("Cookie: {{cookie}}")
    sb.appendLine()
    requestBody?.let { sb.appendLine(requestBody.toJson(false)).appendLine() }
    sb.append(END_FIX)
    return sb.toString()
}

/**
 * 获取测试文件目录
 *
 * @param qName 方法的position
 */
@JvmName("getHttpTestPath")
private fun Project.getHttpTestFile(qName: String, isCreate: Boolean = true): File {
    return buildString {
        append("$basePath${File.separator}.http${File.separator}")
        append(qName.replaceFirst(Regex("#\\w+"), "").replace('.', File.separatorChar))
        append(".http")
    }.let { File(it) }.apply {
        if (!exists() && isCreate) {
            parentFile.mkdirs(); createNewFile()
        }
    }
}

@JvmName("createHttpTestEvnFile")
private fun Project.createHttpTestEvnFile(): File {
    return File("$basePath${File.separator}.http${File.separator}http-client.env.json").apply { if (exists()) return this }
        .apply { parentFile.mkdirs();createNewFile() }.apply { appendText(http_client_evn) }
}

private const val http_client_evn = """
{
  "dev": {
    "host": "http://localhost",
    "cookie": "",
    "token": ""
  },
  "release": {
    "host": "https://www.baidu.com",
    "cookie": "",
    "token": ""
  }
}
"""