// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.github.optimisticgeek.spring.constant

import com.intellij.openapi.util.IconLoader
import com.intellij.spring.mvc.jam.RequestMethod
import com.intellij.ui.scale.JBUIScale
import com.intellij.util.IconUtil
import javax.swing.Icon
import javax.swing.JLabel

/**
 * HttpMethodType

 * @author OptimisticGeek
 * @date 2024/2/14
 */
enum class HttpMethodType {
    ALL,
    GET,
    POST,
    PUT,
    DELETE,
    HEAD,
    OPTIONS,
    TRACE,
    PATCH;

    val icon: Icon = getIconByPath(name) ?: IconUtil.textToIcon(name.first().toString(), JLabel(), JBUIScale.scale(10.0f))

    companion object {
        @JvmStatic
        val LIST = arrayListOf(ALL, GET, POST, PUT, DELETE)
    }

}
private fun getIconByPath(name: String): Icon? {
    return try {
        IconLoader.getIcon("/icon/method/${name}.png", HttpMethodType::class.java)
    } catch (e: Exception) {
        null
    }
}

fun RequestMethod.getIcon(): Icon {
    return HttpMethodType.valueOf(this.name).icon
}