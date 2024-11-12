package com.github.optimistic.editor.action

import com.github.optimistic.analyze.model.AnalyzeModel
import com.github.optimistic.editor.listener.toJsonSchema
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * 复制为Curl
 */
abstract class AbsCopyJsonSchemaAction(key: String) : AbsCopyAction(key) {

    override fun actionPerformed(e: AnActionEvent) = e.copyString(getModel()?.toJsonSchema())

    override fun isVisible(): Boolean = getModel() != null
}

class CopyRequestJsonSchemaAction : AbsCopyJsonSchemaAction("document.requestBody") {

    override fun getModel(): AnalyzeModel? = method?.requestBody
}

class CopyResponseJsonSchemaAction : AbsCopyJsonSchemaAction("document.response") {

    override fun getModel(): AnalyzeModel? = method?.response
}

class CopyQueryParamsJsonSchemaAction : AbsCopyJsonSchemaAction("document.queryParams") {

    override fun getModel(): AnalyzeModel? = method?.queryParams
}

class CopyPathParamsJsonSchemaAction : AbsCopyJsonSchemaAction("document.pathParams") {

    override fun getModel(): AnalyzeModel? = method?.pathParams
}

class CopyModelJsonSchemaAction : AbsCurrentCopyAction("action.copyJsonSchema.current") {

    override fun actionPerformed(e: AnActionEvent) = e.copyString(getModel()?.toJsonSchema())
}