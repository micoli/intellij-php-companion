package org.micoli.php.ui.panels.symfonyProfiler.db

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JTable
import kotlin.arrayOf
import org.micoli.php.service.SqlUtils
import org.micoli.php.symfony.profiler.SymfonyProfileService
import org.micoli.php.symfony.profiler.parsers.DBData
import org.micoli.php.symfony.profiler.parsers.DBQuery
import org.micoli.php.ui.panels.symfonyProfiler.AbstractProfilePanel
import org.micoli.php.ui.table.DoubleCellRenderer
import org.micoli.php.ui.table.MultiLineTableCellRenderer
import org.micoli.php.ui.table.ObjectTableModel

class ProfileDBPanel(val project: Project) : AbstractProfilePanel() {
    lateinit var tableModel: ObjectTableModel<DBQuery>
    lateinit var cardLayout: CardLayout
    lateinit var cardPanel: JBPanel<JBPanel<*>>

    override fun getMainPanel(): JBPanel<*> {
        cardLayout = CardLayout()
        cardPanel = JBPanel<JBPanel<*>>(cardLayout)
        val mainPanel = JBPanel<JBPanel<*>>(BorderLayout())
        val columnNames = arrayOf("Sequence", "Time", "Connection", "SQL")
        tableModel = object : ObjectTableModel<DBQuery>(columnNames) {}
        val table =
            JBTable(tableModel).apply {
                setShowColumns(true)
                setShowGrid(true)
                isStriped = true

                columnModel.getColumn(0).apply {
                    preferredWidth = 20
                    maxWidth = 20
                    minWidth = 20
                }
                columnModel.getColumn(1).apply {
                    preferredWidth = 70
                    maxWidth = 70
                    minWidth = 70
                    cellRenderer = DoubleCellRenderer(decimals = 6)
                }
                columnModel.getColumn(2).apply {
                    preferredWidth = 70
                    maxWidth = 70
                    minWidth = 70
                }
                columnModel.getColumn(3).apply {
                    cellRenderer = MultiLineTableCellRenderer { SqlUtils.Companion.formatSql(it) }
                }
                autoResizeMode = JTable.AUTO_RESIZE_LAST_COLUMN
            }
        table.addMouseListener(
            object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    val row = table.rowAtPoint(e.getPoint())
                    val col = table.columnAtPoint(e.getPoint())
                    when {
                        (e.clickCount == 1 && row >= 0 && col == 3) ->
                            showDetail(tableModel.getObjectAt(row) ?: return)
                        (e.clickCount == 2 && row >= 0) ->
                            showDetail(tableModel.getObjectAt(row) ?: return)
                    }
                }
            })

        val listPanel =
            JBPanel<JBPanel<*>>(BorderLayout()).apply {
                add(JBScrollPane(table), BorderLayout.CENTER)
            }

        cardPanel.add(listPanel, LIST_VIEW)
        mainPanel.add(cardPanel, BorderLayout.CENTER)
        return mainPanel
    }

    override fun refresh() {
        val symfonyProfileService = SymfonyProfileService.getInstance(project)
        symfonyProfileService.loadProfilerDumpPage(DBData::class.java, symfonyProfileDTO.token) {
            setQueries(it?.queries ?: return@loadProfilerDumpPage)
        }
    }

    private fun showDetail(dbQuery: DBQuery?) {
        if (dbQuery == null) {
            return
        }
        cardPanel.components.filterIsInstance<SqlDetailPanel>().forEach { cardPanel.remove(it) }
        cardPanel.add(SqlDetailPanel(project, dbQuery) { showList() }, DETAIL_VIEW)
        cardLayout.show(cardPanel, DETAIL_VIEW)
    }

    private fun showList() {
        cardLayout.show(cardPanel, LIST_VIEW)
    }

    private fun clearQueries() {
        while (tableModel.rowCount > 0) {
            tableModel.removeRow(0)
        }
    }

    fun setQueries(queries: List<DBQuery>?) {
        clearQueries()
        if (queries == null) {
            return
        }
        var index = 0
        for (query in queries) {
            tableModel.addRow(query, arrayOf(index++, query.executionMS, "", query.sql))
        }
        showList()
    }

    companion object {
        private const val LIST_VIEW = "list"
        private const val DETAIL_VIEW = "detail"
    }
}
