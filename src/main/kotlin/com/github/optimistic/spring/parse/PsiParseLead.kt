package com.github.optimistic.spring.parse

import org.jetbrains.annotations.PropertyKey

private const val BUNDLE = "messages.Scanner"

/**
 * 线索类
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class PsiParseLead(
    @PropertyKey(resourceBundle = BUNDLE) val value: String,
    val policy: LeadPolicy = LeadPolicy.PARENT
) {}

/**
 * 线索传递方式
 */
public enum class LeadPolicy() {
    /**
     * 不使用线索
     */
    NONE,

    /**
     * 祖级线索
     */
    NEW,

    /**
     * 父级线索
     */
    PARENT
}
