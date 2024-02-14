// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.github.optimisticgeek.editor.listener

import com.github.optimisticgeek.editor.search.UrlSearchEveryModel
import com.github.optimisticgeek.spring.constant.HttpMethodType
import com.github.optimisticgeek.spring.service.ScannerBundle
import com.github.optimisticgeek.spring.service.SpringScannerService
import com.intellij.ide.actions.searcheverywhere.*
import com.intellij.ide.util.gotoByName.FilteringGotoByModel
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.util.ProgressIndicatorUtils
import com.intellij.openapi.project.PossiblyDumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.codeStyle.MinusculeMatcher
import com.intellij.psi.codeStyle.NameUtil
import com.intellij.util.Processor

// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

/**
 * SpringApiSearchEverywhereClassifier

 * @author OptimisticGeek
 * @date 2024/2/13
 */
class SpringApiSearchEverywhereClassifier(val event: AnActionEvent) : AbstractGotoSEContributor(event),
    PossiblyDumbAware {

    private val myService = event.project!!.service<SpringScannerService>()
    private val myFilter = createFilter()

    override fun getGroupName(): String = ScannerBundle.message("search.title")

    override fun getFullGroupName(): String = groupName

    override fun getSortWeight(): Int = 200

    override fun createModel(project: Project): FilteringGotoByModel<*> = UrlSearchEveryModel(project, myFilter)

    /**
     * 填充搜索结果
     */
    override fun fetchWeightedElements(
        pattern: String,
        progressIndicator: ProgressIndicator,
        consumer: Processor<in FoundItemDescriptor<Any>>
    ) {
        val matcher = createMatcher(this.filterControlSymbols(pattern))
        ProgressIndicatorUtils.yieldToPendingWriteActions()
        ProgressIndicatorUtils.runInReadActionWithWriteActionPriority({
            myService.scanning {
                it.methodMap?.values
                    ?.filter { myFilter.isSelected(it.requestMethod) || myFilter.isSelected(HttpMethodType.ALL) }
                    ?.filter { matcher.matches(it.getKeyword()) }
                    ?.forEach { consumer.process(FoundItemDescriptor(it, 100)) }
            }
        }, progressIndicator)
    }


    /**
     * 搜索窗体的自定义action
     */
    override fun getActions(onChanged: Runnable): MutableList<AnAction> =
        arrayListOf(SearchEverywhereFiltersAction(myFilter, onChanged))

    internal class Factory : SearchEverywhereContributorFactory<Any> {
        override fun createContributor(anActionEvent: AnActionEvent): SearchEverywhereContributor<Any> =
            SpringApiSearchEverywhereClassifier(anActionEvent)
    }

    /**
     * 按HttpMethodType过滤
     */
    private fun createFilter(): PersistentSearchEverywhereContributorFilter<HttpMethodType> {
        return PersistentSearchEverywhereContributorFilter(
            HttpMethodType.LIST, HttpMethodFilterConfiguration.getInstance(myProject), HttpMethodType::name,
            HttpMethodType::icon
        )
    }
}

private fun createMatcher(searchString: String): MinusculeMatcher {
    return NameUtil.buildMatcher("*$searchString").build()
}