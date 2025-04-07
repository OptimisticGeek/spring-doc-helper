// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.github.optimistic.spring.service

import com.github.optimistic.spring.index.isControllerModule
import com.github.optimistic.spring.index.listControllerClass
import com.github.optimistic.spring.model.HttpMethodModel
import com.github.optimistic.spring.parse.ObjClassKey
import com.github.optimistic.spring.parse.printDebugLog
import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolder
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.ProjectScope
import java.util.function.Function
import java.util.function.Supplier
import javax.swing.Icon

/**
 *  使用Spring Mvc相关包解析
 * @author OptimisticGeek
 * @date 2024/2/19
 */
@Service(Service.Level.PROJECT)
class SpringApiService(private val myProject: Project) : Disposable {
    private val javaPsiFacade: JavaPsiFacade = JavaPsiFacade.getInstance(myProject)
    private val projectScope = ProjectScope.getProjectScope(myProject)
    private val moduleManager = myProject.service<ModuleManager>()
    private val dumbService = myProject.service<DumbService>()
    val myModules: HashSet<Module> = hashSetOf()

    fun initModules(): List<Module> {
        return runReadAction {
            myModules.clear()
            moduleManager.getModifiableModel().modules
                .filter { it.isControllerModule() }
                .distinct()
                .forEach(myModules::add)
            printDebugLog { "modules:${myModules.size},${myModules.joinToString(",") { it.name }}" }
            myModules.toList()
        }
    }

    /**
     * 获取所有方法
     */
    fun searchMethods(
        modules: Collection<Module> = if (myModules.isEmpty()) initModules() else myModules,
        progressIndicator: ProgressIndicator? = null,
        limit: Int = 100,
        filter: Function<HttpMethodModel, Boolean>? = null
    ): List<HttpMethodModel> {
        var list: List<HttpMethodModel>? = null
        dumbService.runReadActionInSmartMode(Computable {
            val t = System.currentTimeMillis()

            list = myProject.listControllerClass(modules.ifEmpty { initModules() })
                .flatMap { it.cache.values }
                .filter { progressIndicator?.checkCanceled(); filter?.apply(it) != false }
                .take(limit)

            if (System.currentTimeMillis() - t > 100)
                thisLogger().warn("SpringApiService searchMethods cost: ${System.currentTimeMillis() - t}ms")
        })
        return list ?: return emptyList()
    }

    override fun dispose() {

    }
}

/**
 * psiElement的userData会丢失数据，关联紧密的类，请使用PsiFile的userData
 */
@JvmName("getUserData")
fun <R> PsiClass.getFileUserData(key: Key<R>, default: Supplier<R>? = null): R? =
    default?.let { this.containingFile.getUserData(key, default) } ?: this.containingFile.getUserData(key)

@JvmName("getUserData")
fun <R> UserDataHolder.getUserData(key: Key<R>, default: Supplier<R>): R? =
    this.getUserData(key) ?: default.get()?.also { this.putUserData(key, it) }

@JvmName("clearUserData")
fun <T> UserDataHolder.clearUserData(key: Key<T>) = this.putUserData(key, null)

@JvmName("clearUserData")
fun PsiClass.clearUserData() {
    // 接口或接口方法更新，需要清除方法的cache
    this.getFileUserData(httpMethodModelMapKey)?.let { it.keys.forEach { it.clearUserData(httpMethodModelKey) } }
    // 清除文件的methodMap
    this.containingFile.clearUserData(httpMethodModelMapKey)
    // 更新对象
    this.getFileUserData(ObjClassKey)?.refresh(true)
}

@JvmName("getIcon")
fun Module.getIcon(): Icon = AllIcons.Actions.ModuleDirectory

@JvmName("scannerService")
fun Project.springApiService(): SpringApiService = this.service()

@JvmField
val httpMethodModelKey = Key.create<HttpMethodModel>("HTTP_METHOD_MODEL")

@JvmField
val httpMethodModelMapKey = Key.create<Map<PsiMethod, HttpMethodModel>>("HTTP_METHOD_MODEL_MAP")
