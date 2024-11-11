package com.github.optimistic.editor.action

import com.intellij.openapi.actionSystem.AnActionEvent
import org.apache.commons.lang3.StringUtils

/**
 * 复制为url
 */
class CopyUrlAction : AbsCopyAction("action.copy.url") {

    override fun actionPerformed(e: AnActionEvent) {
        e.copyString(method?.getUrl())
    }

    override fun isVisible(): Boolean {
        return StringUtils.isNotBlank(method?.getUrl())
    }
}