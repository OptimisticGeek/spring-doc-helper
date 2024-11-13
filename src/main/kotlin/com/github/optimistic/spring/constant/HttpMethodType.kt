// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.github.optimistic.spring.constant

import com.intellij.openapi.util.IconLoader
import com.intellij.ui.scale.JBUIScale
import com.intellij.util.IconUtil
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO
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

    val icon: Icon =
        getIconByPath(name) ?: IconUtil.textToIcon(name.first().toString(), JLabel(), JBUIScale.scale(10.0f))

    fun getIconBase64(): String = IconUtil.toBufferedImage(icon).convertToBase64()

    fun BufferedImage.convertToBase64(): String {
        // 使用use函数来自动管理ByteArrayOutputStream的关闭
        ByteArrayOutputStream().use { byteArrayOutputStream ->
            return "data:image/png;base64," + try {
                // 将BufferedImage写入字节数组输出流，格式为PNG
                ImageIO.write(this, "png", byteArrayOutputStream)
                // 获取字节数组并进行Base64编码
                val imageBytes = byteArrayOutputStream.toByteArray()
                Base64.getEncoder().encodeToString(imageBytes) // 返回Base64字符串
            } catch (e: Exception) {
                e.printStackTrace() // 打印异常信息
                "" // 如果发生异常，返回null
            }
        }
    }

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