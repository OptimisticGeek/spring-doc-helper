// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.github.optimisticgeek.editor.search

import com.github.optimisticgeek.spring.constant.HttpMethodType
import com.github.optimisticgeek.spring.model.MethodModel
import com.intellij.ide.actions.searcheverywhere.PersistentSearchEverywhereContributorFilter
import com.intellij.ide.util.gotoByName.FilteringGotoByModel
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.project.Project
import com.intellij.util.ArrayUtil
import com.intellij.util.Processor
import com.intellij.util.indexing.FindSymbolParameters

/**
 * UrlSearchEveryModel

 * @author OptimisticGeek
 * @date 2024/2/14
 */
class UrlSearchEveryModel(project: Project, myFilter: PersistentSearchEverywhereContributorFilter<HttpMethodType>): FilteringGotoByModel<MethodModel>(project, arrayOfNulls(0)) {

    override fun getPromptText(): String {
        return "getPromptText"
    }

    override fun getNotInMessage(): String {
        return "getNotInMessage"
    }

    override fun getNotFoundMessage(): String {
        return "getNotFoundMessage"
    }

    override fun getCheckBoxName(): String? {
        return "getCheckBoxName"
    }

    override fun loadInitialCheckBoxState(): Boolean {
        return true
    }

    override fun saveInitialCheckBoxState(p0: Boolean) {

    }

    override fun getSeparators(): Array<String> {
        return ArrayUtil.EMPTY_STRING_ARRAY
    }

    override fun getFullName(p0: Any): String? {
        return "getFullName"
    }

    override fun willOpenEditor(): Boolean {
        return true
    }

    override fun filterValueFor(p0: NavigationItem?): MethodModel? {
        return null
    }

    override fun acceptItem(item: NavigationItem?): Boolean {
        return super.acceptItem(item)
    }

    override fun processNames(nameProcessor: Processor<in String>, parameters: FindSymbolParameters) {
        super.processNames(nameProcessor, parameters)
    }
}