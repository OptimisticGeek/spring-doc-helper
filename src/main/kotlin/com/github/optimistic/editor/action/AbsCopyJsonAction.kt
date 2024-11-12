package com.github.optimistic.editor.action

import com.github.optimistic.analyze.model.AnalyzeModel
import com.github.optimistic.editor.listener.toJson
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * 复制为Curl
 */
abstract class AbsCopyJsonAction(key: String) : AbsCopyAction(key) {

    override fun actionPerformed(e: AnActionEvent) {
        e.copyString(getModel()?.toJson())
    }

    override fun isVisible(): Boolean {
        return getModel() != null
    }
}

class CopyRequestJsonAction : AbsCopyJsonAction("document.requestBody") {

    override fun getModel(): AnalyzeModel? {
        return method?.requestBody
    }
}

class CopyResponseJsonAction : AbsCopyJsonAction("document.response") {

    override fun getModel(): AnalyzeModel? {
        return method?.response
    }
}

class CopyQueryParamsJsonAction : AbsCopyJsonAction("document.queryParams") {

    override fun getModel(): AnalyzeModel? {
        return method?.queryParams
    }
}

class CopyPathParamsJsonAction : AbsCopyJsonAction("document.pathParams") {

    override fun getModel(): AnalyzeModel? {
        return method?.pathParams
    }
}