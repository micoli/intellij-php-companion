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
            (table.model as ObjectTableModel<T>).getObjectAt(table.convertRowIndexToModel(row))
                ?: return label
        label.setText(formatter(elementDTO))

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
