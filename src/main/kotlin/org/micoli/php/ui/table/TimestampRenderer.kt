package org.micoli.php.ui.table

import java.awt.Component
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import javax.swing.JLabel
import javax.swing.JTable
import javax.swing.table.TableCellRenderer

class TimestampRenderer : TableCellRenderer {
    var dateFormat: DateFormat = SimpleDateFormat("HH:mm:ss")

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
        label.setText(
            if (value != null) dateFormat.format(Date(value.toString().toLong() * 1000)) else "")

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
