// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.github.optimisticgeek.spring.service

import com.github.optimisticgeek.spring.model.BaseMethodModel
import com.intellij.icons.AllIcons
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.CachedValuesManager
import com.intellij.spring.mvc.services.SpringMvcUtils
import com.intellij.spring.mvc.utils.getApplicationPaths
import java.util.function.Function
import javax.swing.Icon

/**
 *  使用Spring Mvc相关包解析
 * @author OptimisticGeek
 * @date 2024/2/19
 */
@Service(Service.Level.PROJECT)
class SpringApiService(private val myProject: Project) {
    private val cacheManager = CachedValuesManager.getManager(myProject)
    val modules = myProject.service<ModuleManager>().modules.filter {
        ModuleRootManager.getInstance(it).sourceRoots.isNotEmpty()
    }

    /**
     * 获取所有方法
     */
    fun searchMethods(
        modules: List<Module> = this.modules,
        filter: Function<BaseMethodModel, Boolean>? = null
    ): List<BaseMethodModel> {
        return modules.flatMap { SpringMvcUtils.getUrlMappings(it) }
            .filter { it.method.isNotEmpty() && it.navigationTarget is PsiMethod }
            .map { BaseMethodModel(it) }
            .filter { filter?.apply(it) ?: true }
            .toList()
    }
}

@JvmName("getIcon")
fun Module.getIcon(): Icon = AllIcons.Actions.ModuleDirectory

// todo 存在多配置文件，根路径不一样的问题
@JvmName("getRootUrl")
fun Module.getRootUrl(): String = getApplicationPaths(this).firstOrNull() ?: ""