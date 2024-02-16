// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.github.optimisticgeek.editor.search

import com.github.optimisticgeek.spring.constant.HttpMethodType
import com.github.optimisticgeek.spring.service.ScannerBundle
import com.github.optimisticgeek.spring.service.SpringScannerService
import com.intellij.find.FindManager
import com.intellij.find.FindModel
import com.intellij.ide.actions.searcheverywhere.*
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.util.ProgressIndicatorUtils
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.codeStyle.MinusculeMatcher
import com.intellij.psi.codeStyle.NameUtil
import com.intellij.util.Consumer
import com.intellij.util.Processor
import com.intellij.util.containers.ContainerUtil
import javax.swing.ListCellRenderer


/**
 * SpringApiSearchEverywhereClassifier

 * @author OptimisticGeek
 * @date 2024/2/13
 */
class SpringApiSearchEverywhereClassifier(event: AnActionEvent) : WeightedSearchEverywhereContributor<SpringApiItem>,
    DumbAware, Disposable {
    private val myProject = event.project!!
    private val myService = myProject.service<SpringScannerService>()
    private val findManager = FindManager.getInstance(myProject)

    /**
     * 搜索模式，正则、单词、大小写这些
     */
    private val myFindModel = findManager.findInProjectModel

    /**
     * 搜索缓存
     */
    private val searchCache: SearchCache = SearchCache()

    /**
     * search everyWhere的管理器
     */
    private val searchManager = SearchEverywhereManager.getInstance(myProject)

    // httpMethod过滤器
    private val myFilter = PersistentSearchEverywhereContributorFilter(
        HttpMethodType.LIST,
        HttpMethodFilterConfiguration.getInstance(myProject),
        HttpMethodType::name,
        HttpMethodType::icon
    )

    private val myListRenderer = SpringApiListCellRenderer()

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
     * 输入框右侧文案
     */
    override fun getAdvertisement(): String? =
        if (isBuiltInMatching()) null else ScannerBundle.message("search.advertisement")

    /**
     * 填充搜索结果
     */
    override fun fetchWeightedElements(
        pattern: String,
        progressIndicator: ProgressIndicator, consumer: Processor<in FoundItemDescriptor<SpringApiItem>>
    ) {
        if (!isEmptyPatternSupported && pattern.isEmpty()) return
        progressIndicator.checkCanceled()
        FindModel.initStringToFind(myFindModel, pattern)
        val matcher = createMatcher(this.filterControlSymbols(pattern))
        ProgressIndicatorUtils.yieldToPendingWriteActions()
        ProgressIndicatorUtils.runInReadActionWithWriteActionPriority({
            // 判断搜索关键字是否刷新，关键字变更会搜索列表
            searchCache.updateResult(pattern) { result ->
                myService.scanning {
                    progressIndicator.checkCanceled()
                    it.takeIf { myFilter.isSelected(HttpMethodType.ALL) || myFilter.isSelected(it.requestMethod) }
                        ?.let { SpringApiItem(it) }
                        ?.takeIf {
                            if (pattern.isEmpty()) return@takeIf true
                            if (!isBuiltInMatching()) return@takeIf it.isFoundString(matcher)
                            return@takeIf it.isFoundString(findManager.findString(it.title, 0, myFindModel))
                        }
                        ?.let { result.add(FoundItemDescriptor(it, it.weight)) }
                }
            }
            searchCache.readResult().let { ContainerUtil.process(it, consumer) }
        }, progressIndicator)
    }

    private fun isBuiltInMatching(): Boolean =
        myFindModel.isCaseSensitive || myFindModel.isRegularExpressions || myFindModel.isWholeWordsOnly

    /**
     * 搜索窗体的自定义action
     */
    override fun getActions(firstOnChanged: Runnable): MutableList<AnAction> {
        // todo 原本区分大小写、正则、单词匹配应该在编辑框中，但是为了兼容低版本
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

        val onChanged = Runnable { searchCache.clear(); firstOnChanged.run() }
        return arrayListOf(
            CaseSensitiveAction(case, onChanged),
            WordAction(word, onChanged),
            RegexpAction(regexp, onChanged),
            SearchEverywhereFiltersAction(myFilter, onChanged)
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

private data class SearchCache(var keyword: String? = null) {
    private val limit = 30
    private var fromIndex: Int = 0
    private var toIndex = limit
    private val result: ArrayList<FoundItemDescriptor<SpringApiItem>> = arrayListOf()

    fun updateResult(
        keyword: String?, keywordChange: Consumer<in ArrayList<FoundItemDescriptor<SpringApiItem>>>? = null
    ) {
        if (keyword == this.keyword && result.size > 0) return
        this.clear()
        this.keyword = keyword
        keywordChange?.consume(result)
    }

    fun clear() {
        this.keyword = null
        this.fromIndex = 0
        this.result.clear()
    }

    fun readResult(): MutableList<FoundItemDescriptor<SpringApiItem>> {
        if (result.isEmpty()) return arrayListOf()
        if (fromIndex > result.size - 1) fromIndex = 0
        toIndex = (fromIndex + limit + 1).let { if (it > result.size) result.size else it }
        return result.subList(fromIndex, toIndex).also { fromIndex = toIndex }
    }
}