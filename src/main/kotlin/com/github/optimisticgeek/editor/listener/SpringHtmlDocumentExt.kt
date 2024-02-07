// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimisticgeek.editor.listener

import com.github.optimisticgeek.analyze.model.AnalyzeMethod
import com.github.optimisticgeek.analyze.model.AnalyzeModel
import com.github.optimisticgeek.spring.constant.BOOLEAN
import com.github.optimisticgeek.spring.constant.FieldType
import com.github.optimisticgeek.spring.constant.NUMBER
import com.github.optimisticgeek.spring.constant.STRING
import com.github.optimisticgeek.spring.model.RefClassModel
import com.github.optimisticgeek.spring.model.className
import com.github.optimisticgeek.spring.service.ScannerBundle
import com.intellij.codeInsight.documentation.DocumentationManagerProtocol.PSI_ELEMENT_PROTOCOL
import com.intellij.json.highlighting.JsonSyntaxHighlighterFactory
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.ui.ColorUtil
import kotlinx.html.*
import kotlinx.html.dom.createHTMLDocument
import kotlinx.html.dom.serialize
import kotlinx.html.stream.createHTML

fun AnalyzeMethod.toHtmlDocument(): String {
    val html = createHTMLDocument().html {
        head {
            title(message("document.title"))
            style {
                unsafe { raw(getHtmlStyle()) }
            }
        }
        body {
            p {
                span { +(if(remark.isNullOrBlank()) name else remark)!! }
                if (author != null) {
                    span { +" - by: ${author!!}" }
                }
            }
            div("url") {
                urls.forEach {
                    p("url") { a("$position###$linkKey###") { +"$it " } }
                }
            }

            unsafe {
                if (pathParams != null) {
                    +pathParams.toHtml(pathParamsKey)
                }
                if (queryParams != null) {
                    +queryParams.toHtml(queryParamsKey)
                }
                if (requestBody != null) {
                    +requestBody.toHtml(requestBodyKey)
                }
                if (response != null) {
                    +response.toHtml(responseKey)
                }
            }
        }
    }
    return html.serialize(false)
}

fun AnalyzeModel.toHtmlDocument(): String {
    val toHtml = this.toHtml(modelKey)

    val html = createHTMLDocument().html {
        head {
            title(message("document.title"))
            style {
                unsafe { raw(getHtmlStyle()) }
            }
        }
        body {
            unsafe {
                +toHtml
            }
        }
    }
    return html.serialize(false)
}

private fun message(key: String): String {
    return ScannerBundle.message(key) + ":"
}

private fun TextAttributesKey.getHtmlColor(): String {
    defaultAttributes.foregroundColor ?: return ""
    return ColorUtil.toHtmlColor(defaultAttributes.foregroundColor)
}

fun RefClassModel?.isShowHtmlDocument(): Boolean {
    this ?: return false
    // 基本类型不显示
    if (this.sourceType.isBase) return false
    // 有字段的对象显示 || 泛型为对象
    return !this.source.fields.isNullOrEmpty() || this.ref.isShowHtmlDocument()
}

private fun AnalyzeModel.toHtml(title: String): String {
    return createHTML(false).div("model") {
        span { +message(title) }
        /*
        span { +"${message(title)}  - " }
        a("$position###$title###$commandCopyHtml", classes = "button") { +"Copy" }
        span { +" - " }
        a("$position###$title###$commandCopyJson", classes = "button") { +"Copy JSON" }
        */
        unsafe {
            +toHtml(0, true)
        }
    }
}

private fun AnalyzeModel.toHtml(level: Int, isLastChild: Boolean): String {
    return createHTML(false).div("level${if (level == 0) "_root" else ""}") {
        // 注释在字段上方
        if (type == FieldType.OBJECT) {
            // 类名[备注]
            position?.className()?.let {
                div {
                    // 屏蔽顶级类的超链接
                    ("// ${if (remark.isNullOrBlank() || remark == it) it else "$it[$remark]"}").let {
                        if (level > 0) {
                            a("$PSI_ELEMENT_PROTOCOL$position", "_self", "remark") { +it }
                        } // else span("remark") { +it }
                    }
                }
            }
        } else if (!remark.isNullOrBlank()) div { span("remark") { +"// $remark" } }

        if (name != null) {
            span("field") { +"\"$name\"" }
            b("colon") { +": " }
        }
        if (type.isBase) {
            span(getStyleName()) { +" ${type.defaultValue.toString()}" }
            span("comma") { +if (isLastChild) "" else "," }
            return@div
        }
        // Object || List
        span("brackets") { +if (type == FieldType.LIST) "[ " else "{ " }

        children?.forEachIndexed { index, it ->
            unsafe { raw(it.toHtml(level + 1, index >= children!!.size - 1)) }
        }
        span("brackets") { +if (type == FieldType.LIST) " ]" else " }" }
    }
}

private fun getHtmlStyle(): String {
    return """
                body {
                  // background-color: #434343;
                  margin: 0;
                  padding: 0;
                }
                
                div.url {
                    padding: 10px 0
                }
                a.button{
                    display: inline-block;
                    padding: 1px;
                }
                
                div.model {
                    overflow: hidden; 
                    border-top: 1px solid ${JsonSyntaxHighlighterFactory.JSON_COLON.getHtmlColor()};
                    padding: 10px 0
                }
                div.level_root>div:first-child{
                    display: flex;
                    justify-content: center;
                    align-items: center;
                }
                div.level{
                    padding-left: 20px;
                    border-left: 1px dashed ${JsonSyntaxHighlighterFactory.JSON_LINE_COMMENT.getHtmlColor()};
                }
                
                .string {
                  color: ${JsonSyntaxHighlighterFactory.JSON_STRING.getHtmlColor()};
                }
                .number {
                  color: ${JsonSyntaxHighlighterFactory.JSON_NUMBER.getHtmlColor()};
                }

                .boolean {
                  color: ${JsonSyntaxHighlighterFactory.JSON_PROPERTY_KEY.getHtmlColor()};
                }

                .null {
                  color: ${JsonSyntaxHighlighterFactory.JSON_PROPERTY_KEY.getHtmlColor()};
                }

                .remark {
                  color: ${JsonSyntaxHighlighterFactory.JSON_LINE_COMMENT.getHtmlColor()};
                  margin-left: 10px
                }

                .field {
                  color: ${JsonSyntaxHighlighterFactory.JSON_KEYWORD.getHtmlColor()};
                }

                .del {
                  color: ${JsonSyntaxHighlighterFactory.JSON_LINE_COMMENT.getHtmlColor()};
                  text-decoration: line-through;
                }

                .colon {
                  color: ${JsonSyntaxHighlighterFactory.JSON_COLON.getHtmlColor()};
                }

                .comma {
                  color: ${JsonSyntaxHighlighterFactory.JSON_COLON.getHtmlColor()};
                }
                
                .brackets {
                  color: ${JsonSyntaxHighlighterFactory.JSON_PROPERTY_KEY.getHtmlColor()};
                }
            """
}

private fun AnalyzeModel.getStyleName(): String {
    return when (type) {
        FieldType.INTEGER, FieldType.DOUBLE, FieldType.LONG -> NUMBER

        FieldType.STRING, FieldType.CHAR, FieldType.ENUM, FieldType.DATE -> STRING

        FieldType.BOOLEAN -> BOOLEAN
        else -> ""
    }
}


internal val pathParamsKey = "document.pathParams"
internal val queryParamsKey = "document.queryParams"
internal val requestBodyKey = "document.requestBody"
internal val responseKey = "document.response"
internal val linkKey = "link"
internal val modelKey = "document.model"