package org.micoli.php.ui.panels.symfonyProfiler.db

import java.awt.Component
import javax.swing.JLabel
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer
import org.micoli.php.service.SqlUtils

class MultiLineTableCellRenderer : DefaultTableCellRenderer() {
    private val label = JLabel().apply { isOpaque = true }

    override fun getTableCellRendererComponent(
        table: JTable,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int,
    ): Component {
        val formattedSql = SqlUtils.Companion.formatSql(value?.toString() ?: "")

        label.text =
            """<html>
            <div style="padding: 4px;">
                <pre style="margin: 0; font-family: monospace; font-size: 11px;">$formattedSql</pre>
            </div>
        </html>"""
                .trimIndent()

        label.background = if (isSelected) table.selectionBackground else table.background
        label.foreground = if (isSelected) table.selectionForeground else table.foreground

        val lines = formattedSql.lines()
        val lineHeight = 16
        val padding = 12
        val preferredHeight = (lines.size * lineHeight) + padding

        if (table.getRowHeight(row) != preferredHeight) {
            table.setRowHeight(row, preferredHeight)
        }

        return label
    }
}
