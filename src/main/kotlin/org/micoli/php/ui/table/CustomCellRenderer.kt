package org.micoli.php.ui.table

import java.awt.Component
import javax.swing.JLabel
import javax.swing.JTable
import javax.swing.table.TableCellRenderer

class CustomCellRenderer<T>(val formatter: (T) -> String) : TableCellRenderer {
    override fun getTableCellRendererComponent(
        table: JTable,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int
    ): Component {
        val label = JLabel()
        val elementDTO =
            try {
                @Suppress("UNCHECKED_CAST")
                (table.model as ObjectTableModel<T>).getObjectAt(table.convertRowIndexToModel(row))
                    ?: return label
            } catch (_: NullPointerException) {
                return label
            }

        label.text = formatter(elementDTO)

        label.background = if (isSelected) table.selectionBackground else table.background
        label.foreground = if (isSelected) table.selectionForeground else table.foreground

        label.setOpaque(true)
        return label
    }
}
