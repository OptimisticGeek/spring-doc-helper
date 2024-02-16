// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.github.optimisticgeek.editor.search

import com.github.optimisticgeek.spring.model.MethodModel
import com.github.optimisticgeek.spring.model.className
import com.intellij.find.FindResult
import com.intellij.openapi.util.TextRange
import com.intellij.psi.codeStyle.MinusculeMatcher
import org.apache.commons.lang3.StringUtils

/**
 * SpringApiItem

 * @author OptimisticGeek
 * @date 2024/2/17
 */
data class SpringApiItem(private val method: MethodModel) {
    val icon = method.requestMethod.icon
    val title = method.getUrl() + method.remark.let { if (it.isNullOrBlank()) StringUtils.EMPTY else " - [$it]" }
    val descriptor =
        method.author.let { if (it.isNullOrBlank()) StringUtils.EMPTY else "[$it] - " } + method.position.let { if (it.isNullOrBlank()) StringUtils.EMPTY else "[${it.className()}]" }
    var textRanges: List<TextRange>? = null
    var weight = 100 - method.requestMethod.ordinal

    fun isFoundString(findResult: FindResult): Boolean {
        updateFindRanges(if (findResult.isStringFound) arrayListOf(findResult) else null)
        return findResult.isStringFound
    }

    fun isFoundString(matcher: MinusculeMatcher): Boolean {
        updateFindRanges(if (matcher.matches(title)) matcher.matchingFragments(title) else null)
        return matcher.matches(title)
    }

    private fun updateFindRanges(textRanges: List<TextRange>?) {
        this.textRanges = textRanges?.also { weight -= textRanges.first().startOffset }
    }

    fun navigate(p0: Boolean) = method.psiMethod.navigate(p0)
}