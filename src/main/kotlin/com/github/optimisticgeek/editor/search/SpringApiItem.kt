// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.github.optimisticgeek.editor.search

import com.github.optimisticgeek.spring.model.BaseMethodModel
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
data class SpringApiItem(private val method: BaseMethodModel) {
    private val defaultWeight = 50 - method.httpMethod.ordinal
    val icon = method.httpMethod.icon
    val title = method.url + method.remark.let { if (it.isBlank()) StringUtils.EMPTY else " - [$it]" }
    val descriptor =
        method.author.let { if (it.isBlank()) StringUtils.EMPTY else "[$it] - " } +
                method.position.let { if (it.isBlank()) StringUtils.EMPTY else "[${it.className()}]" } + " "
    var textRanges: List<TextRange>? = null
    var weight = defaultWeight
    fun isFoundString(findResult: FindResult): Boolean {
        updateFindRanges(arrayListOf(findResult))
        return findResult.isStringFound
    }

    fun isFoundString(matcher: MinusculeMatcher): Boolean {
        updateFindRanges(matcher.matchingFragments(title))
        return matcher.matches(title)
    }

    private fun updateFindRanges(textRanges: List<TextRange>?) {
        if (textRanges.isNullOrEmpty()) return
        this.textRanges = textRanges
        this.weight = defaultWeight - textRanges.first().startOffset
    }

    fun navigate(p0: Boolean) = method.psiMethod.navigate(p0)
}