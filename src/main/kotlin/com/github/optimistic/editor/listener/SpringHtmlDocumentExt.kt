// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimistic.editor.listener

import com.github.optimistic.analyze.model.AnalyzeHttpMethod
import com.github.optimistic.analyze.model.AnalyzeModel
import com.github.optimistic.spring.constant.*
import com.github.optimistic.spring.model.RefClassModel
import com.github.optimistic.spring.model.className
import com.github.optimistic.spring.service.ScannerBundle
import com.intellij.codeInsight.documentation.DocumentationManagerProtocol.PSI_ELEMENT_PROTOCOL
import com.intellij.ide.highlighter.JavaFileHighlighter
import com.intellij.psi.JavaTokenType
import com.intellij.psi.tree.IElementType
import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import kotlinx.html.*
import kotlinx.html.dom.createHTMLDocument
import kotlinx.html.dom.serialize
import kotlinx.html.stream.createHTML
import org.apache.commons.lang3.StringUtils.EMPTY

@JvmName("toHtmlDocument")
fun AnalyzeHttpMethod.toHtmlDocument(source: String = EMPTY): String {
    val html = createHTMLDocument().html {
        head {
            title(message("document.title"))
            style {
                unsafe { raw(getHtmlStyle()) }
            }
        }
        body {
            +"{source}"
            hr()
            // API 标题区域
            div("api-title") {
                span { +(if (remark.isNullOrBlank()) name else remark)!! }
                if (author != null) {
                    span("api-author") { +"by ${author!!}" }
                }
            }
            // URL 区域
            div("url") {
                a("$position###$linkKey###") {
                    img(alt = httpMethod.name, src = httpMethod.getIconBase64())
                    +url
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

@JvmName("toHtmlDocument")
fun AnalyzeModel.toHtmlDocument(source: String = EMPTY): String {
    val toHtml = this.toHtml(modelKey)

    val html = createHTMLDocument().html {
        head {
            title(message("document.title"))
            style {
                unsafe { raw(getHtmlStyle()) }
            }
        }
        body {
            +"{source}"
            hr()
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

@JvmName("isShowHtmlDocument")
fun RefClassModel?.isShowHtmlDocument(): Boolean {
    this ?: return false
    // 基本类型不显示
    if (this.sourceType.isBase) return false
    // 有字段的对象显示 || 泛型为对象
    return !this.source.fields.isNullOrEmpty() || this.ref.isShowHtmlDocument()
}

@JvmName("toHtml")
private fun AnalyzeModel.toHtml(title: String): String {
    return createHTML(false).div("model") {
        span("section-title") { +message(title) }
        unsafe {
            +toHtml(0, true)
        }
    }
}

@JvmName("toHtml")
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
                            a("$PSI_ELEMENT_PROTOCOL$position", "_self") {
                                rel = "remark"
                                +it
                            }
                        } else span("remark") { +it }
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
        /* ===== 基础样式 ===== */
        body {
            margin: 0;
            padding: 12px 16px;
            font-family: 'JetBrains Mono', 'Consolas', 'Monaco', monospace;
            line-height: 1.6;
        }
        
        hr {
            border: none;
            border-top: 1px solid ${JavaTokenType.C_STYLE_COMMENT.getHtmlColor()};
        }
        
        /* ===== API 标题区域 ===== */
        .api-title {
            font-size: 14px;
            font-weight: 600;
            margin-bottom: 4px;
            color: ${JavaTokenType.IDENTIFIER.getHtmlColor()};
            letter-spacing: 0.3px;
        }
        
        .api-author {
            font-size: 12px;
            color: ${JavaTokenType.C_STYLE_COMMENT.getHtmlColor()};
            margin-left: 12px;
            font-weight: normal;
            font-style: italic;
        }
        
        /* ===== URL 区域 ===== */
        div.url {
            margin: 5px 0;
            padding: 5px 0;
            border-bottom: 1px dashed ${JavaTokenType.C_STYLE_COMMENT.getHtmlColor()}50;
        }
        
        div.url a {
            display: inline-block;
            align-items: center;
            gap: 10px;
            text-decoration: none;
            font-size: 14px;
            font-weight: 500;
            color: ${JavaTokenType.IDENTIFIER.getHtmlColor()};
            margin-left: 10px;
        }
        
        div.url a:hover {
            color: ${JavaTokenType.PUBLIC_KEYWORD.getHtmlColor()};
        }
        
        div.url img {
            width: 18px;
            height: 18px;
            vertical-align: middle;
        }
        
        /* ===== 参数/模型区域 ===== */
        div.model {
            margin-top: 8px;
        }
        
        /* 区域标题样式 - 用于 Path Parameters / Query Parameters / Request Body / Response 等 */
        .section-title {
            display: block;
            padding: 12px 0 8px 0;
            margin-top: 8px;
            font-weight: 600;
            font-size: 12px;
            text-transform: uppercase;
            letter-spacing: 1.5px;
            border-bottom: 2px solid ${JavaTokenType.PUBLIC_KEYWORD.getHtmlColor()}60;
            color: ${JavaTokenType.IDENTIFIER.getHtmlColor()};
        }
        
        /* ===== 层级结构 ===== */
        div.level_root {
            padding: 4px;
        }
        
        div.level {
            padding-left: 10px;
            margin: 4px 0;
            border-left: 2px solid ${JavaTokenType.C_STYLE_COMMENT.getHtmlColor()}40;
        }
        
        div.level:hover {
            border-left-color: ${JavaTokenType.PUBLIC_KEYWORD.getHtmlColor()}60;
        }
        
        /* ===== 类名/备注行 ===== */
        div.level > div:first-child {
            margin-bottom: 6px;
        }
        
        /* ===== 字段行 ===== */
        div.level > span.field,
        div.level_root > span.field {
            display: inline;
        }
        
        /* ===== 数据类型颜色 ===== */
        .string {
            color: ${JavaTokenType.STRING_LITERAL.getHtmlColor()};
        }
        
        .number {
            color: ${JavaTokenType.DOUBLE_LITERAL.getHtmlColor()};
        }
        
        .boolean {
            color: ${JavaTokenType.BOOLEAN_KEYWORD.getHtmlColor()};
        }
        
        .null {
            color: ${JavaTokenType.C_STYLE_COMMENT.getHtmlColor()};
            font-style: italic;
        }
        
        /* ===== 字段名样式 ===== */
        .field {
            color: ${JavaTokenType.RETURN_KEYWORD.getHtmlColor()};
            font-weight: 500;
        }
        
        /* ===== 备注/注释样式 ===== */
        .remark {
            color: ${JavaTokenType.C_STYLE_COMMENT.getHtmlColor()};
            font-style: italic;
            opacity: 0.9;
        }
        
        a[rel="remark"] {
            color: ${JavaTokenType.C_STYLE_COMMENT.getHtmlColor()};
            text-decoration: none;
            border-bottom: 1px dashed ${JavaTokenType.C_STYLE_COMMENT.getHtmlColor()}60;
            transition: all 0.2s;
        }
        
        a[rel="remark"]:hover {
            color: ${JavaTokenType.PUBLIC_KEYWORD.getHtmlColor()};
            border-bottom-color: ${JavaTokenType.PUBLIC_KEYWORD.getHtmlColor()};
        }
        
        /* ===== 标点符号 ===== */
        .colon {
            color: ${JavaTokenType.COLON.getHtmlColor()};
            margin: 0 2px;
        }
        
        .comma {
            color: ${JavaTokenType.COMMA.getHtmlColor()};
        }
        
        .brackets {
            color: ${JavaTokenType.LBRACKET.getHtmlColor()};
            font-weight: 500;
        }
        
        /* ===== 删除线（废弃字段） ===== */
        .del {
            color: ${JavaTokenType.C_STYLE_COMMENT.getHtmlColor()};
            text-decoration: line-through;
            opacity: 0.6;
        }
        
        /* ===== 空状态提示 ===== */
        .empty-hint {
            color: ${JavaTokenType.C_STYLE_COMMENT.getHtmlColor()};
            font-style: italic;
            padding: 8px 12px;
            text-align: center;
        }
    """
}


val highlighter = JavaFileHighlighter()

@JvmName("getHtmlColor")
private fun IElementType.getHtmlColor(): String {
    val foregroundColor =
        highlighter.getTokenHighlights(this).firstOrNull()?.defaultAttributes?.foregroundColor
            ?: JBColor.ORANGE
    return ColorUtil.toHtmlColor(foregroundColor)
}


@JvmName("getStyleName")
private fun AnalyzeModel.getStyleName(): String {
    return when (type) {
        FieldType.INTEGER, FieldType.DOUBLE, FieldType.LONG -> NUMBER

        FieldType.STRING, FieldType.CHAR, FieldType.ENUM, FieldType.DATE -> STRING

        FieldType.BOOLEAN -> BOOLEAN
        else -> ""
    }
}