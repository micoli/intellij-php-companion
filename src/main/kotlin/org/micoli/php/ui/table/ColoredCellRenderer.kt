package org.micoli.php.ui.table

import java.awt.Color
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JTable
import javax.swing.table.TableCellRenderer

class ColoredCellRenderer(val colorGetter: (String) -> Color?) : TableCellRenderer {
    override fun getTableCellRendererComponent(
        table: JTable,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int
    ): Component {
        val label = JLabel()
        label.text = value.toString()

        label.background = if (isSelected) table.selectionBackground else table.background
        label.foreground =
            colorGetter(value.toString())
                ?: (if (isSelected) table.selectionForeground else table.foreground)

        label.setOpaque(true)
        return label
    }
}
