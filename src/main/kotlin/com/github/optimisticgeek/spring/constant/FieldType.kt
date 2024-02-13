// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimisticgeek.spring.constant

import com.intellij.psi.CommonClassNames.*
import com.intellij.psi.PsiClass

/**
 * FieldType

 * @author OptimisticGeek
 * @date 2023/12/28
 */
enum class FieldType(
    val qName: String,
    val defaultValue: Any? = null,
    val isObj: Boolean = false,
    val isRef: Boolean = false,
    qNames: Set<String>? = null
) {
    INTEGER(JAVA_LANG_INTEGER, 0, qNames = setOf("int", "short", JAVA_LANG_INTEGER, JAVA_LANG_SHORT)),

    STRING(JAVA_LANG_STRING, "\"\""),

    BYTE(JAVA_LANG_BYTE, ""),

    DOUBLE(JAVA_LANG_DOUBLE, 0.0f, qNames = setOf("double", "float", JAVA_LANG_FLOAT, JAVA_LANG_DOUBLE, BIG_DECIMAL)),

    LONG(JAVA_LANG_LONG, 0, qNames = setOf("long", JAVA_LANG_LONG)),

    BOOLEAN(JAVA_LANG_BOOLEAN, true, qNames = setOf("boolean", JAVA_LANG_BOOLEAN)),

    CHAR(JAVA_LANG_CHARACTER, "'a'", qNames = setOf("char", JAVA_LANG_CHAR_SEQUENCE, JAVA_LANG_CHARACTER)),

    DATE(
        JAVA_UTIL_DATE, "\"2022-02-22 22:22:22\"", qNames = setOf(JAVA_UTIL_DATE)

    ) {
        override fun isFieldType(qName: String): Boolean {
            return super.isFieldType(qName) || qName.startsWith("java.time.")
        }
    },

    ENUM("Enum", "\"\""),

    OBJECT(JAVA_LANG_OBJECT, isObj = true),

    /**
     * 表单文件
     */
    MULTIPART_FILE(FORM_FILE),

    LIST(
        qName = JAVA_UTIL_LIST, isRef = true, qNames = setOf(
            JAVA_UTIL_ARRAY_LIST,
            JAVA_UTIL_LINKED_LIST,
            JAVA_UTIL_SET,
            JAVA_UTIL_HASH_SET,
            JAVA_UTIL_SORTED_SET,
            JAVA_UTIL_LINKED_HASH_SET,
            JAVA_UTIL_LIST
        )
    ),

    /**
     * 泛型
     */
    SUBSTITUTE("T", isRef = true) {
        override fun isFieldType(qName: String): Boolean {
            return qName.length == 1
        }
    },

    /**
     * Map当做obj来处理，因为模拟了key字段
     */
    MAP(JAVA_UTIL_MAP, isObj = true, qNames = setOf( JAVA_UTIL_HASH_MAP, JAVA_UTIL_LINKED_HASH_MAP, JAVA_UTIL_CONCURRENT_HASH_MAP, JAVA_UTIL_MAP)),

    /**
     * 排除的类型或无法识别的类
     */
    OTHER("Other") {
        override fun isFieldType(qName: String): Boolean {
            return qName == "void" || qName == "null" || qName.startsWith("javax.") || qName.startsWith(
                "org.spring"
            )
        }
    };

    private val className: String = qName.className()
    val isBase: Boolean = !(this.isObj || this.isRef)
    private val regex = Regex("(${(qNames?.let { it.joinToString("|") + "|" + qName } ?: qName)})(,|$)")

    override fun toString(): String {
        return className
    }

    open fun isFieldType(qName: String): Boolean {
        return qName == this.qName
    }

    open fun isThisOrSupers(superNames: String?): Boolean {
        return superNames?.let { regex.containsMatchIn(it) } ?: return false
    }

    private fun String.className(): String {
        if (!this.contains(".")) return this
        return this.substring(this.lastIndexOf(".") + 1)
    }
}

@JvmName("getType")
fun getType(psiClass: PsiClass?, qualifiedName: String?): FieldType {
    if (psiClass == null && qualifiedName.isNullOrBlank()) return FieldType.OTHER
    val qName = qualifiedName ?: psiClass?.qualifiedName ?: return FieldType.OTHER
    if (qName.length == 1) return FieldType.SUBSTITUTE
    if (psiClass?.isEnum == true) return FieldType.ENUM

    val supers = "$qName," + psiClass?.let {
        psiClass.interfaces.mapNotNull { it.qualifiedName }
            .joinToString(",") + "," + psiClass.supers.mapNotNull { it.qualifiedName }
            .filter { it != JAVA_LANG_OBJECT }.joinToString(",")
    }
    return FieldType.values().firstOrNull { it.isFieldType(qName) || it.isThisOrSupers(supers) } ?: FieldType.OBJECT
}
