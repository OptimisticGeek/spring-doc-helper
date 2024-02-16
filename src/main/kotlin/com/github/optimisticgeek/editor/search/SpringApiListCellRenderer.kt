// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.github.optimisticgeek.editor.search

import com.intellij.ui.CellRendererPanel
import com.intellij.ui.JBColor
import com.intellij.ui.SimpleColoredComponent
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.speedSearch.SpeedSearchUtil
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.JList
import javax.swing.ListCellRenderer

/**
 * SpringApiListCellRenderer

 * @author OptimisticGeek
 * @date 2024/2/15
 */
class SpringApiListCellRenderer : ListCellRenderer<SpringApiItem> {
    private val myHighlighted = SimpleTextAttributes(
        UIUtil.getListBackground(), UIUtil.getListForeground(), null, SimpleTextAttributes.STYLE_SEARCH_MATCH
    )
    private val mySelected = SimpleTextAttributes(
        UIUtil.getListSelectionBackground(true),
        UIUtil.getListSelectionForeground(true),
        JBColor.RED,
        SimpleTextAttributes.STYLE_PLAIN
    )

    private val myPlain = SimpleTextAttributes(
        UIUtil.getListBackground(), UIUtil.getListForeground(), JBColor.RED, SimpleTextAttributes.STYLE_PLAIN
    )

    private var myLeftRenderer = SimpleColoredComponent().apply {
        isOpaque = false
    }

    private var myRightRenderer = SimpleColoredComponent().apply {
        isOpaque = false
    }

    private val myPanel = CellRendererPanel(BorderLayout()).apply {
        isOpaque = true
        add(myLeftRenderer, BorderLayout.WEST)
        add(myRightRenderer, BorderLayout.EAST)
        border = JBUI.Borders.empty(5)
    }

    override fun getListCellRendererComponent(
        list: JList<out SpringApiItem>, value: SpringApiItem, index: Int, isSelected: Boolean, cellHasFocus: Boolean
    ): Component {
        myLeftRenderer.apply {
            clear()
            icon = value.icon
            SpeedSearchUtil.appendColoredFragments(
                this, value.title, value.textRanges ?: listOf(),
                if (isSelected) mySelected else myPlain, if (isSelected) myHighlighted else myPlain
            )
        }
        myRightRenderer.apply {
            clear()
            append(value.descriptor)
            foreground = if (isSelected) UIUtil.getActiveTextColor() else UIUtil.getInactiveTextColor()
        }

        return myPanel.apply {
            background = if (isSelected) UIUtil.getListSelectionBackground(true) else UIUtil.getListBackground()
        }
    }
}
