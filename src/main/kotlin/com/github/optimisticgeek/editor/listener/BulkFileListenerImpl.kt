// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimisticgeek.editor.listener

import com.github.optimisticgeek.spring.service.clearUserData
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.FileIndexUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil

/**
 * 监听文件刷新
 *
 * @author renFuQiang
 * @date 2021/12/6
 */
class BulkFileListenerImpl(private val project: Project) : BulkFileListener {
    override fun before(events: MutableList<out VFileEvent>) {
        super.before(events)
    }

    override fun after(events: MutableList<out VFileEvent>) {
        if (thisLogger().isDebugEnabled) return
        // todo 未知原因导致，PsiClass的fields更改不生效，例如id字段修改为非id字段，再还原，取到的字段还是修改后的字段，而不是id
        events.filter { it.isFromSave }
            .mapNotNull { it.file }
            .filter { FileIndexUtil.isJavaSourceFile(project, it) && it.fileType is JavaFileType }
            .forEach { it.toPsiClass(project)?.clearUserData() }
    }
}


@JvmName("toPsiClass")
fun VirtualFile.toPsiClass(project: Project): PsiClass? {
    return PsiManager.getInstance(project).findFile(this)
        ?.let { PsiTreeUtil.getChildOfAnyType(it.originalElement, PsiClass::class.java) }
}