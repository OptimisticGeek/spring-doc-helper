package com.github.optimistic.editor.listener

import com.github.optimistic.spring.service.SpringApiService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

/**
 * StartupActivityImpl

 * @author OptimisticGeek
 * @date 2024/2/23
 */
class StartupActivityImpl : ProjectActivity {

    override suspend fun execute(project: Project) {
        project.service<SpringApiService>().searchMethods()
    }
}