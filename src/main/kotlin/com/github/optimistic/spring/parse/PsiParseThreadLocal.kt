package com.github.optimistic.spring.parse

import com.github.optimistic.spring.model.PsiParseThreadTrack
import com.github.optimistic.spring.service.clearUserData
import com.intellij.psi.PsiElement

/**
 * 线程变量
 */
class PsiParseThreadLocal<T : PsiElement> {
    private val threadLocalValue: ThreadLocal<T> = ThreadLocal()

    fun getCurrentPsi(): T? = threadLocalValue.get()

    /**
     * 设置新值，将会清空之前的值
     */
    fun setValue(psi: T, value: PsiParseThreadTrack?) {
        threadLocalValue.set(psi)
        value?.also { psi.putUserData(ParseTrackKey, it) }
    }

    fun getParseTrack(): PsiParseThreadTrack? {
        return threadLocalValue.get()?.getUserData(ParseTrackKey)
    }

    fun clear() {
        getCurrentPsi()?.clearUserData(ParseTrackKey).also { threadLocalValue.remove() }
    }
}

@JvmField
val parseThreadTrack: PsiParseThreadLocal<PsiElement> = PsiParseThreadLocal()