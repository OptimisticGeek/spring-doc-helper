// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.


package com.github.optimistic.editor.search

import com.intellij.find.FindBundle
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.actionSystem.ex.TooltipDescriptionProvider
import com.intellij.openapi.actionSystem.ex.TooltipLinkProvider
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import javax.swing.Icon
import javax.swing.JComponent

/**
 * @see com.intellij.find.impl.TextSearchRightActionAction 来自JetBrains

 * @author OptimisticGeek
 * @date 2024/2/17
 */
open class TextSearchRightAnAction(
    val message: String,
    icon: Icon,
    hoverIcon: Icon,
    selectedIcon: Icon,
    val property: AtomicBooleanProperty,
    val callback: Runnable
) : ToggleAction(message, null, icon), TooltipLinkProvider, TooltipDescriptionProvider {
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    init {
        templatePresentation.hoveredIcon = hoverIcon
        templatePresentation.selectedIcon = selectedIcon
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        return property.get()
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        property.set(state)
        callback.run()
    }

    override fun getTooltipLink(owner: JComponent?): TooltipLinkProvider.TooltipLink? {
        return null
    }
}


class CaseSensitiveAction(property: AtomicBooleanProperty, onChanged: Runnable) : TextSearchRightAnAction(
    FindBundle.message("find.popup.case.sensitive"),
    AllIcons.Actions.MatchCase, AllIcons.Actions.MatchCaseHovered, AllIcons.Actions.MatchCaseSelected,
    property,
    onChanged

) {
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}


class WordAction(property: AtomicBooleanProperty, onChanged: Runnable) : TextSearchRightAnAction(
    FindBundle.message("find.whole.words"),
    AllIcons.Actions.Words, AllIcons.Actions.WordsHovered, AllIcons.Actions.WordsSelected,
    property,
    onChanged
) {
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}


class RegexpAction(property: AtomicBooleanProperty, onChanged: Runnable) : TextSearchRightAnAction(
    FindBundle.message("find.regex"),
    AllIcons.Actions.Regex,
    AllIcons.Actions.RegexHovered,
    AllIcons.Actions.RegexSelected,
    property,
    onChanged
) {
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
