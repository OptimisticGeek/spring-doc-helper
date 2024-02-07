// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimisticgeek.spring.service

import com.github.optimisticgeek.spring.constant.FieldType
import com.github.optimisticgeek.spring.ext.*
import com.github.optimisticgeek.spring.model.ClassModel
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.search.ProjectScope
import com.intellij.psi.search.searches.AllClassesSearch

/**
 * SpringScannerService
 *
 * @author OptimisticGeek
 * @date 2023/12/24
 */
@Service(Service.Level.PROJECT)
class SpringScannerService(private val myProject: Project) {
    val cacheMap: HashMap<String, ClassModel> = HashMap(888)
    private val javaPsiFacade: JavaPsiFacade = JavaPsiFacade.getInstance(myProject)
    private val psiDocumentManager: PsiDocumentManager = myProject.service<PsiDocumentManager>()

    fun scanning() {
        psiDocumentManager.commitAndRunReadAction {
            val t = System.currentTimeMillis()
            val list = AllClassesSearch.search(ProjectScope.getProjectScope(myProject), myProject).findAll()
                .filter { it.isControllerClass() }.mapNotNull { it.createControllerModel() }.toList()
            var toList = list.map { it.analyze() }.toList()
            println(list)
            thisLogger().warn("${System.currentTimeMillis() - t}")
        }
    }

    fun findClassModel(qName: String): ClassModel? {
        if (qName == "null" || qName == "void") return null

        FieldType.getType(null, qName).takeIf { it != FieldType.OBJECT }
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

fun PsiClass?.clearModelCache(){
    this?.let { if(it.isControllerClass()) it.clearControllerCache() else it.clearClassModelCache() }
}

fun PsiClass.clearClassModelCache() {
    project.scannerService().cacheMap[this.qualifiedName]?.also { it.setDefaultStatus() }
}

fun PsiElement.scannerService(): SpringScannerService {
    return this.project.service<SpringScannerService>()
}

fun Project.scannerService(): SpringScannerService {
    return this.service<SpringScannerService>()
}