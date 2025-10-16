package org.micoli.php.ui.table

import java.awt.Component
import javax.swing.JLabel
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer
import org.micoli.php.service.HtmlStyle.Companion.getHtmlCss

class MultiLineTableCellRenderer(val formatter: (String) -> String) : DefaultTableCellRenderer() {

    override fun getTableCellRendererComponent(
        table: JTable,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int,
    ): Component {
        val formattedText = formatter(value?.toString() ?: "")
        val label = JLabel().apply { isOpaque = true }
        val style = getHtmlCss()
        label.text =
            """<html>
                $style
            <div style="padding: 4px;">
                <pre style="margin: 0; font-family: monospace; font-size: 11px;">$formattedText</pre>
            </div>
        </html>"""
                .trimIndent()

        label.background = if (isSelected) table.selectionBackground else table.background
        label.foreground = if (isSelected) table.selectionForeground else table.foreground

        setHeight(formattedText, table, row)

        return label
    }

    private fun setHeight(formattedText: String, table: JTable, row: Int) {
        val lines = formattedText.lines()
        val lineHeight = 16
        val padding = 12
        val preferredHeight = (lines.size * lineHeight) + padding

        if (table.getRowHeight(row) != preferredHeight) {
            table.setRowHeight(row, preferredHeight)
        }
    }
}
