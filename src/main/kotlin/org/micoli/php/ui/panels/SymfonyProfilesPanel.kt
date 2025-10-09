package org.micoli.php.ui.panels

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import java.lang.String
import javax.swing.RowSorter
import javax.swing.SortOrder
import javax.swing.SwingUtilities
import javax.swing.SwingWorker
import javax.swing.table.TableRowSorter
import org.micoli.php.symfony.profiler.SymfonyProfileDTO
import org.micoli.php.symfony.profiler.SymfonyProfileService
import org.micoli.php.ui.SymfonyWindowContent
import org.micoli.php.ui.table.AbstractListPanel
import org.micoli.php.ui.table.ActionIconRenderer
import org.micoli.php.ui.table.ObjectTableModel
import org.micoli.php.ui.table.TimestampRenderer

class SymfonyProfilesPanel(project: Project, val symfonyWindowContent: SymfonyWindowContent) :
    AbstractListPanel<SymfonyProfileDTO>(project, "symfonyProfiles", COLUMN_NAMES) {
    override fun getSorter(): TableRowSorter<ObjectTableModel<SymfonyProfileDTO>> {
        innerSorter = TableRowSorter<ObjectTableModel<SymfonyProfileDTO>>(model)
        innerSorter.setSortKeys(
            listOf<RowSorter.SortKey?>(
                RowSorter.SortKey(0, SortOrder.DESCENDING),
                RowSorter.SortKey(1, SortOrder.ASCENDING),
            ))
        innerSorter.setComparator(0, String.CASE_INSENSITIVE_ORDER)
        innerSorter.setComparator(1, String.CASE_INSENSITIVE_ORDER)
        innerSorter.setComparator(2, String.CASE_INSENSITIVE_ORDER)
        innerSorter.setComparator(3, String.CASE_INSENSITIVE_ORDER)
        innerSorter.setComparator(4, Comparator { _: Any?, _: Any? -> 0 })
        return innerSorter
    }

    override fun configureTableColumns() {
        table.getColumnModel().getColumn(0).setCellRenderer(TimestampRenderer())
        table.getColumnModel().getColumn(0).setMaxWidth(85)
        table.getColumnModel().getColumn(1).setMaxWidth(60)
        table.getColumnModel().getColumn(2).setMaxWidth(800)
        table.getColumnModel().getColumn(3).setMaxWidth(60)
        table.getColumnModel().getColumn(4).setCellRenderer(ActionIconRenderer())
        table.getColumnModel().getColumn(4).setMinWidth(50)
        table.getColumnModel().getColumn(4).setMaxWidth(50)
    }

    override fun handleActionDoubleClick(elementDTO: SymfonyProfileDTO) {
        BrowserUtil.open(elementDTO.profileUrl)
    }

    override fun handleActionLineSelected(elementDTO: SymfonyProfileDTO) {
        symfonyWindowContent.profileSelected(elementDTO)
    }

    override fun refresh() {
        synchronized(lock) {
            try {
                table.emptyText.text = "Loading profiles, please wait..."
                clearItems()

                val localTable = table
                val localModel = model
                val localProject = project

                val worker: SwingWorker<Void?, SymfonyProfileDTO> =
                    object : SwingWorker<Void?, SymfonyProfileDTO>() {
                        override fun doInBackground(): Void? {
                            ApplicationManager.getApplication().runReadAction {
                                for (item in
                                    SymfonyProfileService.getInstance(localProject).elements) {
                                    publish(item)
                                }
                            }
                            return null
                        }

                        override fun process(chunks: MutableList<SymfonyProfileDTO>) {
                            SwingUtilities.invokeLater {
                                for (item in chunks) {
                                    localModel.addRow(
                                        item,
                                        arrayOf(
                                            item.timestamp,
                                            item.method,
                                            item.url,
                                            item.statusCode,
                                            null))
                                }
                            }
                        }

                        override fun done() {
                            SwingUtilities.invokeLater {
                                localTable.emptyText.text = "Nothing to show"
                                localModel.fireTableDataChanged()
                            }
                        }
                    }
                worker.execute()
            } catch (e: Exception) {
                logger.error("Error refreshing profilers " + e.localizedMessage, e)
            }
        }
    }

    override fun getColumnCount(): Int {
        return COLUMN_NAMES.size
    }

    companion object {
        private val COLUMN_NAMES = arrayOf("Timestamp", "Method", "Path", "Code", "Action")
    }
}
