package com.github.optimisticgeek.editor.listener

import com.github.optimisticgeek.spring.service.SpringApiService
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity

// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

/**
 * StartupActivityImpl

 * @author OptimisticGeek
 * @date 2024/2/23
 */
class StartupActivityImpl : StartupActivity.RequiredForSmartMode {
    override fun runActivity(project: Project) {
        val service = project.service<SpringApiService>()
        service.searchMethods()
            .apply { runReadAction { service.myModules = this.map { it.myModule }.distinct().toList() } }
    }
}