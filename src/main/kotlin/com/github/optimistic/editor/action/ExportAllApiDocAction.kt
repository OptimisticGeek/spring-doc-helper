package com.github.optimistic.editor.action

import com.github.optimistic.analyze.model.toCurlStr
import com.github.optimistic.editor.listener.analyze
import com.github.optimistic.editor.listener.copyString
import com.github.optimistic.spring.service.springApiService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * 导出所有接口文档
 */
class ExportAllApiDocAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val sb = StringBuilder()
        project.springApiService().searchMethods { method ->
            val analyze = method.analyze()
            sb.appendLine(analyze.toCurlStr())
            return@searchMethods true
        }
        project.copyString(sb.toString())
    }
}