package com.github.optimisticgeek.editor.listener

import com.github.optimisticgeek.spring.service.clearClassModelCache
import com.intellij.ide.highlighter.JavaClassFileType
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
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
    override fun before(events: List<VFileEvent>) {
    }

    override fun after(events: List<VFileEvent>) {
        if (thisLogger().isDebugEnabled) {
            return
        }
        events.mapNotNull { it.file }
            .filter { it.fileType is JavaClassFileType || it.fileType is JavaFileType }
            .forEach { it.toPsiClass(project)?.clearClassModelCache() }
    }
}

fun VirtualFile.toPsiClass(project: Project): PsiClass? {
    return PsiManager.getInstance(project).findFile(this)
        ?.let { PsiTreeUtil.getChildOfAnyType(it.originalElement, PsiClass::class.java) }
}