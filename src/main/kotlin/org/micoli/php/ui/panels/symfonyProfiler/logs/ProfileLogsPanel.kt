package org.micoli.php.ui.panels.symfonyProfiler.logs

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import java.awt.BorderLayout
import javax.swing.JTable
import javax.swing.RowSorter
import javax.swing.SortOrder
import javax.swing.table.TableRowSorter
import kotlin.apply
import kotlin.arrayOf
import org.micoli.php.symfony.profiler.SymfonyProfileService
import org.micoli.php.symfony.profiler.parsers.Log
import org.micoli.php.symfony.profiler.parsers.LoggerData
import org.micoli.php.ui.panels.symfonyProfiler.AbstractProfilePanel
import org.micoli.php.ui.table.AbstractListPanel
import org.micoli.php.ui.table.MultiLineTableCellRenderer
import org.micoli.php.ui.table.ObjectTableModel

class ProfileLogsPanel(project: Project) : AbstractProfilePanel(project) {
    val table =
        object :
            AbstractListPanel<Log>(
                project, "logs", arrayOf("Time", "Severity", "Channel", "Message", "context")) {
            override fun getSorter(): TableRowSorter<ObjectTableModel<Log>> {
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
                    isStriped = true

                    columnModel.getColumn(0).apply {
                        preferredWidth = 100
                        maxWidth = 100
                        minWidth = 100
                    }
                    columnModel.getColumn(1).apply {
                        preferredWidth = 80
                        minWidth = 80
                    }
                    columnModel.getColumn(2).apply {
                        preferredWidth = 80
                        minWidth = 80
                    }
                    columnModel.getColumn(3).apply {
                        preferredWidth = 200
                        minWidth = 100
                    }
                    columnModel.getColumn(4).apply {
                        minWidth = 200
                        cellRenderer = MultiLineTableCellRenderer { it }
                    }
                    autoResizeMode = JTable.AUTO_RESIZE_LAST_COLUMN
                }
            }

            override fun refresh() {
                ApplicationManager.getApplication().executeOnPooledThread {
                    SymfonyProfileService.getInstance(project)
                        .loadProfilerDumpPage(
                            LoggerData::class.java,
                            symfonyProfileDTO.token,
                            loaderLogCallback(System.nanoTime()),
                            { showError(it) },
                            {
                                synchronized(lock) {
                                    val model = table.model as ObjectTableModel<Log>
                                    while (model.rowCount > 0) {
                                        model.removeRow(0)
                                    }
                                    for (log in it?.logs ?: return@loadProfilerDumpPage) {
                                        model.addRow(
                                            log,
                                            arrayOf(
                                                log.time,
                                                log.severity,
                                                log.channel,
                                                log.message,
                                                log.context))
                                    }
                                    showMainPanel()
                                }
                            })
                }
            }
        }

    init {
        mainPanel.add(table, BorderLayout.CENTER)
        initialize()
    }

    override fun refresh() {
        table.refresh()
    }
}
