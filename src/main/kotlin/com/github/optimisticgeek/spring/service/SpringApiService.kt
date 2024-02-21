// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.github.optimisticgeek.spring.service

import com.github.optimisticgeek.spring.model.MethodModel
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.util.ModificationTracker
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.spring.mvc.mapping.UrlMappingElement
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

    fun allMethods(psiClass: PsiClass): Map<PsiMethod, MethodModel> {
        return cacheManager.getCachedValue(psiClass) {
            CachedValueProvider.Result.create(
                searchMethods { it.psiClass == psiClass }.associateBy { it.psiMethod },
                ModificationTracker { psiClass.containingFile.modificationStamp }
            )
        } ?: mapOf()
    }

    /**
     * 获取所有方法
     */
    fun searchMethods(
        modules: List<Module> = myProject.getSourceModules(),
        limit: Int? = null,
        filter: Function<MethodModel, Boolean>? = null
    ): List<MethodModel> {
        return runReadAction {
            modules.asSequence()
                .flatMap { SpringMvcUtils.getUrlMappings(it) }
                .mapNotNull { it.createMethodModel() }
                .filter { filter?.apply(it) ?: true }
                .take(limit ?: Int.MAX_VALUE)
                .toList()
        }
    }

    @JvmName("createMethodModel")
    private fun UrlMappingElement.createMethodModel(): MethodModel? {
        if (method.isNullOrEmpty() || navigationTarget == null || navigationTarget !is PsiMethod) return null
        return navigationTarget?.let {
            cacheManager.getCachedValue(it) {
                CachedValueProvider.Result.create(
                    MethodModel(this),
                    ModificationTracker { it.containingFile.modificationStamp }
                )
            }
        }
    }
}

@JvmName("getIcon")
fun Module.getIcon(): Icon = AllIcons.Actions.ModuleDirectory

// todo 存在多配置文件，根路径不一样的问题
@JvmName("getRootUrl")
fun Module.getRootUrl(): String = getApplicationPaths(this).firstOrNull() ?: ""

@JvmName("getSourceModules")
fun Project.getSourceModules() = this.service<ModuleManager>().modules.filter {
    ModuleRootManager.getInstance(it).sourceRoots.isNotEmpty()
}

@JvmName("listHttpMethod")
fun PsiClass.listHttpMethod(): Map<PsiMethod, MethodModel> = project.service<SpringApiService>().allMethods(this)

@JvmName("getHttpMethod")
fun PsiMethod.getHttpMethod(): MethodModel? =
    this.containingClass?.let { project.service<SpringApiService>().allMethods(it)[this] }
