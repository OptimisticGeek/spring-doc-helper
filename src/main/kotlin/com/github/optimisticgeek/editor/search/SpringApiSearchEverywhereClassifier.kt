// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.github.optimisticgeek.editor.search

import com.github.optimisticgeek.spring.constant.HttpMethodType
import com.github.optimisticgeek.spring.model.HttpMethodModel
import com.github.optimisticgeek.spring.service.ScannerBundle
import com.github.optimisticgeek.spring.service.SpringApiService
import com.github.optimisticgeek.spring.service.getIcon
import com.github.optimisticgeek.spring.service.springApiService
import com.intellij.find.FindManager
import com.intellij.find.FindModel
import com.intellij.ide.actions.searcheverywhere.*
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.module.Module
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.util.ProgressIndicatorUtils
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.codeStyle.MinusculeMatcher
import com.intellij.psi.codeStyle.NameUtil
import com.intellij.util.Processor
import javax.swing.ListCellRenderer


/**
 * SpringApiSearchEverywhereClassifier

 * @author OptimisticGeek
 * @date 2024/2/13
 */
class SpringApiSearchEverywhereClassifier(event: AnActionEvent) : WeightedSearchEverywhereContributor<SpringApiItem>,
    PossibleSlowContributor,
    Disposable {
    private val myProject = event.project!!

    private val myListRenderer = SpringApiListCellRenderer()
    private val myFilter = MyFilter(myProject)

    override fun isEmptyPatternSupported(): Boolean = true

    override fun getSearchProviderId(): String = "springDocHelper.api"

    /**
     * tab切页面上的名称
     */
    override fun getGroupName(): String = ScannerBundle.message("search.title")

    /**
     * ALL页面上的名称
     */
    override fun getFullGroupName(): String = ScannerBundle.message("search.title")

    /**
     * 是否显示在单独的tab中
     */
    override fun isShownInSeparateTab(): Boolean = true

    /**
     * tabs展示顺序，权重越小越靠前
     */
    override fun getSortWeight(): Int = 200

    /**
     * 是否显示在“查找窗口”中显示结果
     */
    override fun showInFindResults(): Boolean = false

    override fun getDataForItem(p0: SpringApiItem, p1: String): SpringApiItem? = null

    /**
     * 点击列表后的操作，true关闭窗体，false不关闭
     */
    override fun processSelectedItem(selected: SpringApiItem, modifiers: Int, searchText: String): Boolean =
        selected.navigate(true).let { return true }


    /**
     * 创建左右列表渲染器
     */
    override fun getElementsRenderer(): ListCellRenderer<in SpringApiItem> = myListRenderer

    /**
     * 填充搜索结果
     */
    override fun fetchWeightedElements(
        pattern: String,
        progressIndicator: ProgressIndicator,
        consumer: Processor<in FoundItemDescriptor<SpringApiItem>>
    ) {
        if (DumbService.isDumb(myProject)) return
        if (myFilter.moduleFilter.selectedElements.isEmpty()) return
        if (!isEmptyPatternSupported && pattern.isEmpty()) return
        progressIndicator.checkCanceled()
        FindModel.initStringToFind(myFilter.findModel, pattern)
        val matcher = createMatcher(this.filterControlSymbols(pattern))
        ProgressIndicatorUtils.runInReadActionWithWriteActionPriority({
            myProject.service<SpringApiService>()
                .searchMethods(myFilter.myModules, progressIndicator) { myFilter.match(it, matcher, consumer) }
        }, progressIndicator)
    }

    /**
     * 搜索窗体的自定义action
     */
    override fun getActions(onChanged: Runnable): MutableList<AnAction> {
        // todo 原本区分大小写、正则、单词匹配应该在编辑框中，但是为了兼容低版本
        val myFindModel = myFilter.findModel
        val case = AtomicBooleanProperty(myFindModel.isCaseSensitive).apply {
            afterChange { myFindModel.isCaseSensitive = it }
        }
        lateinit var regexp: AtomicBooleanProperty
        val word = AtomicBooleanProperty(myFindModel.isWholeWordsOnly).apply {
            afterChange { myFindModel.isWholeWordsOnly = it; if (it) regexp.set(false) }
        }

        regexp = AtomicBooleanProperty(myFindModel.isRegularExpressions).apply {
            afterChange { myFindModel.isRegularExpressions = it; if (it) word.set(false) }
        }

        return arrayListOf(
            CaseSensitiveAction(case, onChanged),
            WordAction(word, onChanged),
            RegexpAction(regexp, onChanged),
            SearchEverywhereFiltersAction(myFilter.moduleFilter, onChanged),
            SearchEverywhereFiltersAction(myFilter.methodFilter, onChanged)
        )
    }

    /**
     * 拼音匹配器
     */
    private fun createMatcher(searchString: String): MinusculeMatcher =
        NameUtil.buildMatcher("*${searchString.replace(' ', '*')}").build()

    internal class Factory : SearchEverywhereContributorFactory<SpringApiItem> {
        override fun createContributor(anActionEvent: AnActionEvent): SearchEverywhereContributor<SpringApiItem> =
            SpringApiSearchEverywhereClassifier(anActionEvent)
    }

    override fun isDumbAware(): Boolean = false
}

private class MyFilter(myProject: Project) {
    val findManager: FindManager = FindManager.getInstance(myProject)
    val findModel = findManager.findInProjectModel
    val myModules get() = moduleFilter.selectedElements.toList()
    val allModules = myProject.springApiService().let { it.myModules.ifEmpty { it.initModules(); it.myModules } }

    // httpMethod过滤器
    val methodFilter = PersistentSearchEverywhereContributorFilter(
        HttpMethodType.LIST,
        myProject.service<HttpMethodFilterConfiguration>(),
        HttpMethodType::name,
        HttpMethodType::icon
    )

    // httpMethod过滤器
    val moduleFilter = PersistentSearchEverywhereContributorFilter(
        allModules.toList(),
        myProject.service<ModuleFilterConfiguration>(),
        Module::getName,
        Module::getIcon
    )

    fun match(
        model: HttpMethodModel, matcher: MinusculeMatcher, consumer: Processor<in FoundItemDescriptor<SpringApiItem>>
    ): Boolean {
        FindModel.initStringToFind(findModel, matcher.pattern)
        if (!methodFilter.isSelected(HttpMethodType.ALL) && !methodFilter.isSelected(model.httpMethod)) return false
        SpringApiItem(model).takeIf {
            if (matcher.pattern.isEmpty()) return@takeIf true
            if (!isBuiltInMatching()) return@takeIf it.isFoundString(matcher)
            return@takeIf it.isFoundString(findManager.findString(it.title, 0, findModel))
        }?.let { consumer.process(FoundItemDescriptor(it, it.weight)) } ?: return false
        return true
    }

    fun isBuiltInMatching(): Boolean =
        findModel.isCaseSensitive || findModel.isRegularExpressions || findModel.isWholeWordsOnly
}