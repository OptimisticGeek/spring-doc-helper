// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.github.optimistic.editor.search

import com.github.optimistic.spring.constant.HttpMethodType
import com.intellij.ide.util.gotoByName.ChooseByNameFilterConfiguration
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage


/**
 * UrlSearchEveryWhereConfiguration

 * @author OptimisticGeek
 * @date 2024/2/14
 */
@Service(Service.Level.PROJECT)
@State(name = "HttpMethodFilterConfiguration", storages = [Storage("\$WORKSPACE_FILE$")])
class HttpMethodFilterConfiguration : ChooseByNameFilterConfiguration<HttpMethodType>() {
    override fun nameForElement(p0: HttpMethodType): String = p0.name
}
