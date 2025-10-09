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
import org.micoli.php.symfony.profiler.models.DBQueries
import org.micoli.php.symfony.profiler.models.DBQuery
import org.micoli.php.ui.table.ObjectTableModel

class ProfileDBPanel(val project: Project) : JBPanel<ProfileDBPanel>(BorderLayout()) {
    var model: ObjectTableModel<DBQuery>
    private val cardLayout = CardLayout()
    private val cardPanel = JBPanel<JBPanel<*>>(cardLayout)

    init {
        val columnNames = arrayOf("Sequence", "Time", "Connection", "SQL")
        model = object : ObjectTableModel<DBQuery>(columnNames) {}
        val table =
            JBTable(model).apply {
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
                columnModel.getColumn(3).apply { cellRenderer = MultiLineTableCellRenderer() }
                autoResizeMode = JTable.AUTO_RESIZE_LAST_COLUMN
            }
        table.addMouseListener(
            object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    val row = table.rowAtPoint(e.getPoint())
                    val col = table.columnAtPoint(e.getPoint())
                    if (e.clickCount == 2 && row >= 0 && col == 3) {
                        showDetail(model.getObjectAt(row))
                        return
                    }
                }
            })

        val listPanel =
            JBPanel<JBPanel<*>>(BorderLayout()).apply {
                add(JBScrollPane(table), BorderLayout.CENTER)
            }

        cardPanel.add(listPanel, LIST_VIEW)
        add(cardPanel, BorderLayout.CENTER)
    }

    private fun showDetail(dbQuery: DBQuery) {
        cardPanel.components.filterIsInstance<SqlDetailPanel>().forEach { cardPanel.remove(it) }
        val detailPanel = SqlDetailPanel(project, dbQuery) { showList() }
        cardPanel.add(detailPanel, DETAIL_VIEW)
        cardLayout.show(cardPanel, DETAIL_VIEW)
    }

    private fun showList() {
        cardLayout.show(cardPanel, LIST_VIEW)
    }

    private fun clearQueries() {
        while (model.rowCount > 0) {
            model.removeRow(0)
        }
    }

    fun setQueries(queries: DBQueries?) {
        clearQueries()
        if (queries == null) {
            return
        }
        var index = 0
        for (connection in queries.queries.entries) {
            for (query in connection.value) {
                model.addRow(query, arrayOf(index++, query.executionMS, connection.key, query.sql))
            }
        }
        showList()
    }

    companion object {
        private const val LIST_VIEW = "list"
        private const val DETAIL_VIEW = "detail"
    }
}
