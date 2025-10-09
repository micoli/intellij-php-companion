package org.micoli.php.ui.panels.symfonyProfiler.db

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.JLabel
import org.micoli.php.service.SqlUtils
import org.micoli.php.symfony.profiler.models.DBQuery
import org.micoli.php.ui.panels.symfonyProfiler.BackTraceTable

class SqlDetailPanel(project: Project, private val dbQuery: DBQuery, onBack: () -> Unit) :
    JBPanel<SqlDetailPanel>(BorderLayout()) {

    init {
        border = JBUI.Borders.empty(10)
        val headerPanel =
            JBPanel<JBPanel<*>>(BorderLayout()).apply { border = JBUI.Borders.emptyBottom(10) }

        headerPanel.add(Link("back to list", onBack), BorderLayout.WEST)

        val infoLabel =
            JLabel().apply {
                text =
                    """<html>
                <body style="font-family: sans-serif; padding: 10px;">
                    <h3 style="margin: 0 0 10px 0;">Query</h3>
                    <code>${SqlUtils.Companion.formatHtmlSql(dbQuery.sql)}</code>
                </body>
            </html>"""
                        .trimIndent()
            }
        val infoPanel =
            JBPanel<JBPanel<*>>(BorderLayout()).apply { border = JBUI.Borders.emptyBottom(10) }
        infoPanel.add(infoLabel, BorderLayout.NORTH)
        infoPanel.add(BackTraceTable(project, dbQuery.backtrace), BorderLayout.CENTER)
        val scrollPane = JBScrollPane(infoPanel)
        scrollPane.setBorder(JBUI.Borders.empty())

        add(headerPanel, BorderLayout.NORTH)
        add(scrollPane, BorderLayout.CENTER)
    }
}
