package org.micoli.php.ui.panels.symfonyProfiler.db

import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.datatransfer.StringSelection
import org.micoli.php.service.SqlUtils
import org.micoli.php.symfony.profiler.parsers.DBQuery
import org.micoli.php.ui.Link
import org.micoli.php.ui.Notification
import org.micoli.php.ui.panels.symfonyProfiler.BackTraceTable

class SqlDetailPanel(project: Project, private val dbQuery: DBQuery, onBack: () -> Unit) :
    JBPanel<SqlDetailPanel>(BorderLayout()) {

    init {
        border = JBUI.Borders.empty(10)
        val headerPanel =
            JBPanel<JBPanel<*>>(BorderLayout()).apply { border = JBUI.Borders.emptyBottom(10) }
        val infoPanel =
            JBPanel<JBPanel<*>>(BorderLayout()).apply { border = JBUI.Borders.emptyBottom(10) }
        val scrollPane = JBScrollPane(infoPanel).apply { border = JBUI.Borders.empty() }

        headerPanel.add(Link("back to list", onBack), BorderLayout.WEST)

        val sqlLabel =
            Link("<code>${SqlUtils.Companion.formatHtmlSql(dbQuery.sql)}</code>") {
                val stringSelection = StringSelection(SqlUtils.Companion.formatSql(dbQuery.sql))
                CopyPasteManager.getInstance().setContents(stringSelection)
                Notification.getInstance(project)
                    .messageWithTimeout("Content copied to clipboard", 500)
            }

        infoPanel.add(sqlLabel, BorderLayout.NORTH)
        infoPanel.add(BackTraceTable(project, dbQuery.backtrace), BorderLayout.CENTER)
        add(headerPanel, BorderLayout.NORTH)
        add(scrollPane, BorderLayout.CENTER)
    }
}
