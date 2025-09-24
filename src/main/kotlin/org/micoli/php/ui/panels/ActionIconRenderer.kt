package org.micoli.php.ui.panels

import com.intellij.icons.AllIcons
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JTable
import javax.swing.table.TableCellRenderer

class ActionIconRenderer : TableCellRenderer {
    override fun getTableCellRendererComponent(
        table: JTable,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int
    ): Component {
        val label = JLabel()
        label.setHorizontalAlignment(JLabel.CENTER)
        label.setIcon(AllIcons.Actions.Lightning)

        if (isSelected) {
            label.setBackground(table.getSelectionBackground())
            label.setForeground(table.getSelectionForeground())
        } else {
            label.setBackground(table.getBackground())
            label.setForeground(table.getForeground())
        }

        label.setOpaque(true)
        return label
    }
}
