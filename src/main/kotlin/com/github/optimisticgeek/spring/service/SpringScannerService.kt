// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimisticgeek.spring.service

import com.github.optimisticgeek.spring.constant.FieldType
import com.github.optimisticgeek.spring.constant.getType
import com.github.optimisticgeek.spring.ext.clearControllerCache
import com.github.optimisticgeek.spring.ext.createControllerModel
import com.github.optimisticgeek.spring.ext.isControllerClass
import com.github.optimisticgeek.spring.ext.toClassModel
import com.github.optimisticgeek.spring.model.ClassModel
import com.github.optimisticgeek.spring.model.MethodModel
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.PossiblyDumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.*
import com.intellij.psi.search.ProjectScope
import com.intellij.psi.search.searches.AllClassesSearch

/**
 * SpringScannerService
 *
 * @author OptimisticGeek
 * @date 2023/12/24
 */
@Service(Service.Level.PROJECT)
class SpringScannerService(private val myProject: Project) : PossiblyDumbAware {
    val cacheMap: HashMap<String, ClassModel> = HashMap(888)
    val methodCache: HashMap<PsiMethod, MethodModel> = HashMap()
    private val javaPsiFacade: JavaPsiFacade = JavaPsiFacade.getInstance(myProject)
    private val psiDocumentManager: PsiDocumentManager = myProject.service<PsiDocumentManager>()
    private val CACHE_KEY_SCANNER_STATE: Key<Boolean> = Key.create("springDocHelper.scanner.state")

    @Synchronized
    fun scanning(useCache: Boolean = true) {
        // 使用缓存 && 已扫描过，直接跳过
        if (useCache && myProject.getUserData(CACHE_KEY_SCANNER_STATE) == true) return
        AllClassesSearch.search(ProjectScope.getProjectScope(myProject), myProject).findAll()
            .filter { it.isControllerClass() }.mapNotNull { it.createControllerModel(useCache) }
            .map {
                it.methodMap?.let { it1 -> methodCache.putAll(it1) }
                it
            }.toList()
        myProject.putUserData(CACHE_KEY_SCANNER_STATE, true)
    }

    fun findClassModel(qName: String): ClassModel? {
        if (qName == "null" || qName == "void") return null

        getType(null, qName).takeIf { it != FieldType.OBJECT }
            ?.let { fieldType -> return buildSourceModel(qName, fieldType) }

        ProjectScope.getProjectScope(myProject).let {
            return cacheMap[qName] ?: javaPsiFacade.findClass(qName, it)?.toClassModel()
        }
    }

    fun buildSourceModel(qName: String?, type: FieldType, useCache: Boolean = true): ClassModel {
        val key = if (type.isBase) type.qName else qName ?: type.qName
        // 不能使用缓存：基于原有classModel，重写属性与字段
        return cacheMap.getOrPut(key) { ClassModel(qName = key, type = type) }
            .also { if (!useCache && type == FieldType.OBJECT) it.setDefaultStatus() }
    }

    fun parseClassModel(it: PsiClass): ClassModel? {
        return it.toClassModel()
    }

}

@JvmName("clearModelCache")
fun PsiClass?.clearModelCache(){
    this?.let { if(it.isControllerClass()) it.clearControllerCache() else it.clearClassModelCache() }
}

@JvmName("clearClassModelCache")
fun PsiClass.clearClassModelCache() {
    project.scannerService().cacheMap[this.qualifiedName]?.also { it.setDefaultStatus() }
}

@JvmName("scannerService")
fun PsiElement.scannerService(): SpringScannerService {
    return this.project.service<SpringScannerService>()
}

@JvmName("scannerService")
fun Project.scannerService(): SpringScannerService {
    return this.service<SpringScannerService>()
}