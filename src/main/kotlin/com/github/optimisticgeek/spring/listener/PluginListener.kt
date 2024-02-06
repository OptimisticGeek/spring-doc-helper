package com.github.optimisticgeek.spring.listener

import com.intellij.ide.plugins.DynamicPluginListener
import com.intellij.ide.plugins.IdeaPluginDescriptor

/**
 * ProjectListener

 * @author OptimisticGeek
 * @date 2023/12/26
 */
class PluginListener : DynamicPluginListener {
    override fun pluginLoaded(pluginDescriptor: IdeaPluginDescriptor) {
        super.pluginLoaded(pluginDescriptor)
    }
}