package org.micoli.php.ui.popup

import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.JLabel
import javax.swing.JList

class NavigableListCellRenderer : DefaultListCellRenderer() {
    override fun getListCellRendererComponent(
        list: JList<*>?,
        value: Any?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean,
    ): Component? {
        if (value is NavigableListPopupItem) {
            return getNavigableItemLabel(list, index, isSelected, cellHasFocus, value)
        }

        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
    }

    private fun getNavigableItemLabel(
        list: JList<*>?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean,
        item: NavigableListPopupItem?,
    ): JLabel {
        if (item is NavigableOpenSearchAction || item is NavigableOpenAllAction) {
            val label =
                super.getListCellRendererComponent(list, 0, index, isSelected, cellHasFocus)
                    as JLabel

            label.setText(item.getText())

            return label
        }
        if (item is NavigableItem) {
            val fileExtract = item.fileExtract
            val label =
                super.getListCellRendererComponent(
                    list, fileExtract.lineNumber, index, isSelected, cellHasFocus) as JLabel

            label.setIcon(item.icon)
            label.setText(item.getText())

            return label
        }
        throw IllegalArgumentException("Unsupported item type")
    }
}
