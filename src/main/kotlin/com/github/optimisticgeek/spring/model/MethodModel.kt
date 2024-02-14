// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimisticgeek.spring.model

import com.github.optimisticgeek.spring.constant.HttpMethodType
import com.github.optimisticgeek.spring.ext.getAuthor
import com.github.optimisticgeek.spring.ext.getHttpMethodType
import com.github.optimisticgeek.spring.ext.getHttpRequestAnnotation
import com.github.optimisticgeek.spring.ext.getRemark
import com.intellij.navigation.ItemPresentation
import com.intellij.navigation.NavigationItem
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiMethod
import org.apache.commons.lang3.StringUtils
import javax.swing.Icon

/**
 * MethodModel

 * @author OptimisticGeek
 * @date 2023/12/27
 */
class MethodModel(val psiMethod: PsiMethod) : BaseModel(
    psiMethod.name, psiMethod.getAuthor(), psiMethod.getRemark(), psiMethod.getPosition()
), NavigationItem {
    val psiMethodAnnotation: PsiAnnotation = psiMethod.getHttpRequestAnnotation()!!
    val requestMethod: HttpMethodType = psiMethodAnnotation.getHttpMethodType() ?: HttpMethodType.GET
    val methodName: String = name!!
    var urls: List<String>? = null
    var isViewer: Boolean? = null
    var requestBody: FieldModel? = null
    val pathVariables = ArrayList<FieldModel>()
    val queryParams = ArrayList<FieldModel>()
    var responseBody: RefClassModel? = null

    // todo 暂未增加对viewer跳转的识别
    fun getUrl(): String {
        return urls?.firstOrNull() ?: StringUtils.EMPTY
    }

    override fun toString(): String {
        return "MethodModel(urls=$urls, isViewer=$isViewer, requestMethod=$requestMethod, psiMethodAnnotation=$psiMethodAnnotation, requestBody=$requestBody, pathVariables=$pathVariables, requestParams=$queryParams, responseBody=$responseBody)"
    }

    override fun navigate(p0: Boolean) = psiMethod.navigate(p0)

    override fun canNavigate(): Boolean = true

    override fun canNavigateToSource(): Boolean = true

    override fun getName(): String = getUrl()

    override fun getPresentation(): ItemPresentation {
        return object : ItemPresentation {
            override fun getPresentableText(): String {
                return getUrl()
            }

            override fun getIcon(p0: Boolean): Icon {
                return requestMethod.icon
            }

            override fun getLocationString(): String {
                return buildString {
                    append((if (remark.isNullOrBlank()) StringUtils.EMPTY else "[$remark] - "))
                    append((if (author.isNullOrBlank()) StringUtils.EMPTY else "[$author] - "))
                    append((if (position.isNullOrBlank()) StringUtils.EMPTY else "[${position!!.className()}]"))
                }
            }
        }
    }

    fun getKeyword(): String = "${getUrl()} ${methodName} $author $remark"
}

@JvmName("getPosition")
fun PsiMethod.getPosition(): String {
    return "${this.containingClass!!.qualifiedName}#$name"
}