package com.github.optimistic.editor.model

import com.github.optimistic.spring.constant.HttpMethodType

class MethodJsonSchema(
    val url: String,
    val method: HttpMethodType,
    val remark: String = "",
    val author: String = "",
    val position: String = ""
) {
    var pathVariables: List<AbsJsonSchema?>? = null
    var queryParams: List<AbsJsonSchema?>? = null
    var requestBody: AbsJsonSchema? = null
    var response: AbsJsonSchema? = null
}