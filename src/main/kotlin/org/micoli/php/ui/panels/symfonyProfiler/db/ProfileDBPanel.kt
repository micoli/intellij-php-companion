package org.micoli.php.ui.panels.symfonyProfiler.db

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBPanel
import java.awt.BorderLayout
import java.awt.CardLayout
import javax.swing.JTable
import javax.swing.RowSorter
import javax.swing.SortOrder
import javax.swing.table.TableRowSorter
import kotlin.apply
import kotlin.arrayOf
import org.micoli.php.service.SqlUtils
import org.micoli.php.symfony.profiler.SymfonyProfileService
import org.micoli.php.symfony.profiler.parsers.DBData
import org.micoli.php.symfony.profiler.parsers.DBQuery
import org.micoli.php.ui.panels.symfonyProfiler.AbstractProfilePanel
import org.micoli.php.ui.table.AbstractListPanel
import org.micoli.php.ui.table.DoubleCellRenderer
import org.micoli.php.ui.table.MultiLineTableCellRenderer
import org.micoli.php.ui.table.ObjectTableModel

class ProfileDBPanel(project: Project) : AbstractProfilePanel(project) {
    val cardLayout: CardLayout = CardLayout()
    val cardPanel: JBPanel<JBPanel<*>> = JBPanel<JBPanel<*>>(cardLayout)
    val table =
        object :
            AbstractListPanel<DBQuery>(project, "dispatches", arrayOf("Sequence", "Time", "SQL")) {
            override fun getSorter(): TableRowSorter<ObjectTableModel<DBQuery>> {
                val innerSorter = TableRowSorter(model)
                innerSorter.setSortKeys(
                    listOf<RowSorter.SortKey?>(
                        RowSorter.SortKey(0, SortOrder.ASCENDING),
                    ))
                innerSorter.setComparator(1, java.lang.String.CASE_INSENSITIVE_ORDER)
                innerSorter.setComparator(2, java.lang.String.CASE_INSENSITIVE_ORDER)
                return innerSorter
            }

            override fun configureTableColumns() {
                table.apply {
                    setShowColumns(true)
                    setShowGrid(true)
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
                        cellRenderer = MultiLineTableCellRenderer {
                            SqlUtils.Companion.formatHtmlSql(it)
                        }
                    }
                    autoResizeMode = JTable.AUTO_RESIZE_LAST_COLUMN
                }
            }

            override fun refresh() {
                ApplicationManager.getApplication().executeOnPooledThread {
                    SymfonyProfileService.getInstance(project)
                        .loadProfilerDumpPage(
                            DBData::class.java,
                            symfonyProfileDTO.token,
                            loaderLogCallback(System.nanoTime()),
                            { showError(it) },
                            {
                                synchronized(lock) {
                                    val model = table.model as ObjectTableModel<DBQuery>
                                    while (model.rowCount > 0) {
                                        model.removeRow(0)
                                    }
                                    var index = 0
                                    for (query in it?.queries ?: return@loadProfilerDumpPage) {
                                        model.addRow(
                                            query,
                                            arrayOf(index++, query.executionMS, query.htmlSql))
                                    }
                                    showMainPanel()
                                    showList()
                                }
                            })
                }
            }

            override fun handleActionDoubleClick(col: Int, elementDTO: DBQuery): Boolean {
                if (col == 2) {
                    showDetail(elementDTO)
                    return true
                }
                return false
            }
        }

    init {
        cardPanel.add(
            JBPanel<JBPanel<*>>(BorderLayout()).apply { add(table, BorderLayout.CENTER) },
            LIST_VIEW)
        mainPanel.add(cardPanel, BorderLayout.CENTER)
        initialize()
    }

    override fun refresh() {
        table.refresh()
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

    companion object {
        private const val LIST_VIEW = "list"
        private const val DETAIL_VIEW = "detail"
    }
}
