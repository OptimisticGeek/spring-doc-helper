package com.github.optimistic.spring.model

import com.github.optimistic.spring.constant.CONTROLLER
import com.github.optimistic.spring.constant.REST_CONTROLLER
import com.github.optimistic.spring.ext.getAuthor
import com.github.optimistic.spring.ext.getHttpRequestAnnotation
import com.github.optimistic.spring.ext.getRemark
import com.github.optimistic.spring.service.httpMethodModelKey
import com.github.optimistic.spring.service.httpMethodModelMapKey
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.intellij.psi.SmartPointerManager

class ControllerModel(psiClass: PsiClass) :
    BaseModel(psiClass.name, psiClass.getAuthor(), psiClass.getRemark().ifEmpty { "" }, psiClass.qualifiedName) {
    val cache: MutableMap<PsiMethod, HttpMethodModel> = mutableMapOf()

    /**
     * 接口的根路径
     */
    val urlPath: UrlPath = UrlPath(psiClass)

    /**
     * @RestController || @Controller
     */
    val isController: Boolean =
        psiClass.getAnnotation(CONTROLLER) != null || psiClass.getAnnotation(REST_CONTROLLER) != null

    /**
     * 来源
     */
    val sourcePsi = SmartPointerManager.createPointer<PsiClass>(psiClass)

    init {
        psiClass.putUserData(httpMethodModelMapKey, cache)
        psiClass.methods.filter { it.getHttpRequestAnnotation() != null }
            .forEach {
                cache.put(it, HttpMethodModel(it, this))
                it.putUserData(httpMethodModelKey, cache[it])
            }
    }
}