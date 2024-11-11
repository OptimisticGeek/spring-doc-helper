// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.github.optimistic.editor.search

import com.intellij.ide.util.gotoByName.ChooseByNameFilterConfiguration
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.module.Module


/**
 * UrlSearchEveryWhereConfiguration

 * @author OptimisticGeek
 * @date 2024/2/14
 */
@Service(Service.Level.PROJECT)
@State(name = "ModuleFilterConfiguration", storages = [Storage("\$WORKSPACE_FILE$")])
class ModuleFilterConfiguration : ChooseByNameFilterConfiguration<Module>() {
    override fun nameForElement(p0: Module): String = p0.name
}
