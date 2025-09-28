package org.micoli.php.ui.panels

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.pom.Navigatable
import java.lang.String
import javax.swing.RowSorter
import javax.swing.SortOrder
import javax.swing.SwingUtilities
import javax.swing.SwingWorker
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableRowSorter
import kotlin.Any
import kotlin.Comparator
import kotlin.Exception
import kotlin.Int
import kotlin.arrayOf
import kotlin.synchronized
import org.micoli.php.symfony.list.CommandElementDTO
import org.micoli.php.symfony.list.CommandService

class CommandsPanel(project: Project) :
    AbstractListPanel<CommandElementDTO?>(project, "commands", COLUMN_NAMES) {
    override fun getSorter(): TableRowSorter<DefaultTableModel> {
        innerSorter = TableRowSorter<DefaultTableModel>(model)
        innerSorter.setSortKeys(
            listOf<RowSorter.SortKey?>(
                RowSorter.SortKey(0, SortOrder.ASCENDING),
                RowSorter.SortKey(1, SortOrder.ASCENDING),
            ))
        innerSorter.setComparator(0, String.CASE_INSENSITIVE_ORDER)
        innerSorter.setComparator(1, String.CASE_INSENSITIVE_ORDER)
        innerSorter.setComparator(2, Comparator { _: Any?, _: Any? -> 0 })
        return innerSorter
    }

    override fun configureTableColumns() {
        table.getColumnModel()?.getColumn(0)?.setMaxWidth(800)
        table.getColumnModel()?.getColumn(1)?.setMaxWidth(200)
        table.getColumnModel()?.getColumn(2)?.setCellRenderer(ActionIconRenderer())
        table.getColumnModel()?.getColumn(2)?.setMinWidth(50)
        table.getColumnModel()?.getColumn(2)?.setMaxWidth(50)
    }

    override fun handleActionClick(row: Int) {
        ApplicationManager.getApplication().invokeLater {
            val elementDTO =
                table.getValueAt(row, getColumnCount() - 1) as CommandElementDTO?
                    ?: return@invokeLater
            (elementDTO.element as? Navigatable)?.navigate(true)
        }
    }

    override fun refresh() {
        synchronized(lock) {
            try {
                table.emptyText.text = "Loading CLI, please wait..."
                clearItems()

                val worker: SwingWorker<Void?, CommandElementDTO> =
                    object : SwingWorker<Void?, CommandElementDTO>() {
                        override fun doInBackground(): Void? {
                            ApplicationManager.getApplication().runReadAction {
                                val commandListService = CommandService.getInstance(project)
                                for (item in commandListService.getElements()) {
                                    publish(item)
                                }
                            }
                            return null
                        }

                        override fun process(chunks: MutableList<CommandElementDTO>) {
                            SwingUtilities.invokeLater {
                                for (item in chunks) {
                                    model.addRow(arrayOf(item.command, item.description, item))
                                }
                            }
                        }

                        override fun done() {
                            SwingUtilities.invokeLater {
                                table.emptyText.text = "Nothing to show"
                                model.fireTableDataChanged()
                            }
                        }
                    }
                worker.execute()
            } catch (e: Exception) {
                LOGGER.error("Error refreshing CLI table", e)
            }
        }
    }

    override fun getColumnCount(): Int {
        return COLUMN_NAMES.size
    }

    companion object {
        private val COLUMN_NAMES = arrayOf("Command", "Description", "Actions")
    }
}
