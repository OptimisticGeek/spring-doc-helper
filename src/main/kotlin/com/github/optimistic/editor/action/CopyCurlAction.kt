package com.github.optimistic.editor.action

import com.github.optimistic.analyze.model.toCurlStr
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * 复制为Curl
 */
class CopyCurlAction : AbsCopyAction("action.copy.curl") {

    override fun actionPerformed(e: AnActionEvent) {
        e.copyString(method?.toCurlStr())
    }

    override fun isVisible(): Boolean {
        return method != null
    }
}