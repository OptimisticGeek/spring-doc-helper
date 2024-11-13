package com.github.optimistic.spring.model

import com.github.optimistic.spring.constant.DEFAULT
import com.github.optimistic.spring.constant.HttpMethodType
import com.github.optimistic.spring.constant.REQUEST_MAPPING
import com.github.optimistic.spring.ext.getAnnotationValue
import com.github.optimistic.spring.ext.getHttpMethodType
import com.github.optimistic.spring.ext.getHttpRequestAnnotation
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.intellij.psi.SmartPointerManager
import org.apache.commons.lang3.StringUtils
import javax.swing.Icon

class UrlPath(psiAnnotation: PsiAnnotation?, val parent: UrlPath? = null) {
    /**
     * 来源
     */
    val sourcePsi = psiAnnotation?.let { SmartPointerManager.createPointer<PsiAnnotation>(it) }

    /**
     * 访问路径
     */
    val url: String

    /**
     * 请求方法
     */
    val httpMethod: HttpMethodType = sourcePsi?.element?.getHttpMethodType() ?: HttpMethodType.ALL

    /**
     * 方法小图标
     */
    val icon: Icon? = httpMethod.icon

    constructor(psiClass: PsiClass) : this(psiClass.getAnnotation(REQUEST_MAPPING))
    constructor(psiMethod: PsiMethod, parent: UrlPath) : this(psiMethod.getHttpRequestAnnotation(), parent)

    init {
        url = sourcePsi?.element?.getAnnotationValue(DEFAULT).let { combineUrls(parent?.url ?: StringUtils.EMPTY, it ?: StringUtils.EMPTY) }
    }

    private fun combineUrls(baseUrl: String, relationUrl: String): String =
        baseUrl.trimEnd('/') + "/" + relationUrl.trimStart('/')
}
