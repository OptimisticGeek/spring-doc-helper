package com.github.optimisticgeek.spring.model

import com.github.optimisticgeek.spring.constant.DEFAULT
import com.github.optimisticgeek.spring.constant.REQUEST_MAPPING
import com.github.optimisticgeek.spring.constant.REST_CONTROLLER
import com.github.optimisticgeek.spring.ext.getAnnotationValues
import com.github.optimisticgeek.spring.ext.getAuthor
import com.github.optimisticgeek.spring.ext.getRemark
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import org.apache.commons.lang3.StringUtils

/**
 * ControllerModel

 * @author OptimisticGeek
 * @date 2023/12/27
 */
class ControllerModel(psiClass: PsiClass) : BaseModel(
    position = psiClass.qualifiedName, name = psiClass.name,
    remark = psiClass.getRemark(), author = psiClass.getAuthor()
) {
    val urls: List<String> = psiClass.getAnnotationValues(REQUEST_MAPPING, DEFAULT)
    val isViewer: Boolean = !psiClass.hasAnnotation(REST_CONTROLLER)
    val qName: String = position!!
    var methodMap: Map<PsiMethod, MethodModel>? = null

    fun getUrl(): String {
        return urls.firstOrNull() ?: StringUtils.EMPTY
    }

    override fun toString(): String {
        return "ControllerModel(urls=$urls, methods=$methodMap, isViewer=$isViewer)"
    }
}


