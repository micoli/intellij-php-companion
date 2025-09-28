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
import org.micoli.php.symfony.list.DoctrineEntityElementDTO
import org.micoli.php.symfony.list.DoctrineEntityService

class DoctrineEntitiesPanel(project: Project) :
    AbstractListPanel<DoctrineEntityElementDTO?>(project, "doctrineEntities", COLUMN_NAMES) {
    override fun getSorter(): TableRowSorter<DefaultTableModel> {
        innerSorter = TableRowSorter<DefaultTableModel>(model)
        innerSorter.setSortKeys(
            listOf<RowSorter.SortKey?>(
                RowSorter.SortKey(0, SortOrder.ASCENDING),
                RowSorter.SortKey(1, SortOrder.ASCENDING),
            ))
        innerSorter.setComparator(0, String.CASE_INSENSITIVE_ORDER)
        innerSorter.setComparator(1, String.CASE_INSENSITIVE_ORDER)
        innerSorter.setComparator(2, String.CASE_INSENSITIVE_ORDER)
        innerSorter.setComparator(3, Comparator { _: Any?, _: Any? -> 0 })
        return innerSorter
    }

    override fun configureTableColumns() {
        table.getColumnModel()?.getColumn(0)?.setMaxWidth(600)
        table.getColumnModel()?.getColumn(1)?.setMaxWidth(200)
        table.getColumnModel()?.getColumn(2)?.setMaxWidth(100)
        table.getColumnModel()?.getColumn(3)?.setCellRenderer(ActionIconRenderer())
        table.getColumnModel()?.getColumn(3)?.setMinWidth(50)
        table.getColumnModel()?.getColumn(3)?.setMaxWidth(50)
    }

    override fun handleActionClick(row: Int) {
        ApplicationManager.getApplication().invokeLater {
            val elementDTO =
                table.getValueAt(row, getColumnCount() - 1) as DoctrineEntityElementDTO?
                    ?: return@invokeLater
            (elementDTO.element as? Navigatable)?.navigate(true)
        }
    }

    override fun refresh() {
        synchronized(lock) {
            try {
                table.emptyText.text = "Loading Entities, please wait..."
                clearItems()

                val worker: SwingWorker<Void?, DoctrineEntityElementDTO> =
                    object : SwingWorker<Void?, DoctrineEntityElementDTO>() {
                        override fun doInBackground(): Void? {
                            ApplicationManager.getApplication().runReadAction {
                                val doctrineEntitiesService =
                                    DoctrineEntityService.getInstance(project)
                                for (item in doctrineEntitiesService.getElements()) {
                                    publish(item)
                                }
                            }
                            return null
                        }

                        override fun process(chunks: MutableList<DoctrineEntityElementDTO>) {
                            SwingUtilities.invokeLater {
                                for (item in chunks) {
                                    model.addRow(
                                        arrayOf(item.className, item.name, item.schema, item))
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
                LOGGER.error("Error refreshing Entities table", e)
            }
        }
    }

    override fun getColumnCount(): Int {
        return COLUMN_NAMES.size
    }

    companion object {
        private val COLUMN_NAMES = arrayOf("Entity", "Table", "Schema", "Actions")
    }
}
