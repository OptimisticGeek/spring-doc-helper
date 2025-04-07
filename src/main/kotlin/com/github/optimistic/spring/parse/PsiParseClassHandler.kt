package com.github.optimistic.spring.parse

import com.github.optimistic.spring.model.ClassType
import com.github.optimistic.spring.model.PsiParseThreadTrack
import com.github.optimistic.spring.model.type.BaseClass
import com.github.optimistic.spring.service.ScannerBundle
import com.intellij.psi.PsiElement
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

/**
 * 线程变量通过 ThreadLocal(存储psi) + PsiUserData(存储变量)
 */
class PsiParseClassHandler(private val target: PsiParseClassService) : InvocationHandler {

    override fun invoke(
        proxy: Any, method: Method, params: Array<out Any?>
    ): Any? {
        val methodName = method.name

        // 方法执行前的切入逻辑
        val parseLead: PsiParseLead =
            method.getAnnotation(PsiParseLead::class.java) ?: return method.invoke(target, *params)
        val funcName = ScannerBundle.message(parseLead.value)

        val psi = params.firstOrNull() as? PsiElement ?: return method.invoke(target, *params)
        val root: ClassType? = params.firstOrNull { it is ClassType } as? ClassType
        // 根据枚举判断作用域
        var track: PsiParseThreadTrack? = when (parseLead.policy) {
            LeadPolicy.NEW -> PsiParseThreadTrack(psi)
            LeadPolicy.PARENT -> parseThreadTrack.getParseTrack()?.create(psi) ?: PsiParseThreadTrack(psi)
            else -> null
        }.apply { parseThreadTrack.setValue(psi, this) }
        // 通过psi的userData获取缓存
        psi.getUserData(ObjClassKey)?.takeIf { method.returnType == BaseClass::class.java }?.apply {
            printDebugLog { "【前置拦截成功】-psi：${this}，终止后续方法调用" }
            track?.finished(this, track.depth == 0)
            return this
        }

        track?.takeIf { it.checkDepth() || it.checkLoopCall() }?.run {
            return if (method.returnType == params.firstOrNull()?.javaClass) root
            else track.finished(null, track.depth == 0)
        }

        // 前置切面，尝试线索填充
        if (track != null && root?.target != null && method.returnType == root.javaClass) {
            root.takeIf { track.refreshFromLeads(root) }?.apply {
                printDebugLog { "【前置拦截成功】-线索：${root}，终止后续方法调用。" }
                track.finished(this, track.depth == 0)
                return root
            }
        }
        printDebugLog { "${track?.depth} - 执行前：$methodName - ${Thread.currentThread().name}" }

        // 调用方法
        var result: Any? = runCatching { method.invoke(target, *params) }.run {
            if (isFailure) onFailure { it.printStackTrace() }
            this

        }.getOrNull()

        printDebugLog { "${track?.depth} - 执行后：$methodName - ${Thread.currentThread().name}\n线索：${track?.leadMap}" }

        track?.also { printDebugLog { "${track.depth} - " + track.threadTrack.toString() } }
        if (result !is BaseClass || track == null) {
            track?.finished(root, track.depth == 0)
            return result
        }

//        if (root is ObjClass) psi.putUserData(ObjClassKey, root)
//            .apply { printDebugLog { "添加Psi缓存：【${root}】 <-- $psi" } }


        track.refreshFromLeads(result)

        // 对象补充完整 或 整体执行结果

        return track.finished(result, track.depth == 0)
    }
}