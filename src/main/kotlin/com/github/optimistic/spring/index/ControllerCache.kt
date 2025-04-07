package com.github.optimistic.spring.index

import com.github.optimistic.spring.constant.CONTROLLER
import com.github.optimistic.spring.constant.REST_CONTROLLER
import com.github.optimistic.spring.ext.className
import com.github.optimistic.spring.model.ControllerModel
import com.github.optimistic.spring.model.HttpMethodModel
import com.github.optimistic.spring.service.getUserData
import com.github.optimistic.spring.service.httpMethodModelKey
import com.github.optimistic.spring.service.springApiService
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.parentOfType

private val instance = JavaAnnotationIndex.getInstance()

@JvmName("listProjectController")
fun Project.listControllerClass(modules: Collection<Module> = springApiService().myModules): Collection<ControllerModel> {
    return modules.flatMap { it.listControllerModel() }
}

/**
 * 获取模块中的所有Controller
 */
@JvmName("listModuleController")
fun Module.listControllerModel(): Collection<ControllerModel> {
    return instance.get(REST_CONTROLLER.className(), project, moduleScope)
        .union(instance.get(CONTROLLER.className(), project, moduleScope))
        .distinct()
        .distinct()
        .mapNotNull { it.toControllerModel() }
}

/**
 * 获取模块中的所有Controller
 */
@JvmName("isControllerModule")
fun Module.isControllerModule(): Boolean {
    return instance.get(REST_CONTROLLER.className(), project, moduleScope).isNotEmpty()
            || instance.get(CONTROLLER.className(), project, moduleScope).isNotEmpty()

}
        /**
 * 获取注解对应的Controller
 */
@JvmName("psiAnnotationController")
fun PsiAnnotation.toControllerModel(): ControllerModel? = parentOfType<PsiClass>()?.toControllerModel()

/**
 * PsiClass 转 ControllerModel
 */
@JvmName("toControllerModel")
fun PsiClass.toControllerModel(): ControllerModel? {
    return CachedValuesManager.getManager(project).getCachedValue(this) {
        ControllerModel(this).takeIf { it.isController }.let { return@getCachedValue CachedValueProvider.Result.create(it, this) }
    }
}

@JvmName("getHttpMethodMap")
fun PsiClass.getHttpMethodMap(): Map<PsiMethod, HttpMethodModel>? = toControllerModel()?.cache

@JvmName("getHttpMethod")
fun PsiMethod.getHttpMethodModel(): HttpMethodModel? = this.getUserData(httpMethodModelKey){
    this.parentOfType<PsiClass>()?.getHttpMethodMap()?.get(this)
}
