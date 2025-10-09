package org.micoli.php.ui.panels

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.pom.Navigatable
import java.lang.String
import javax.swing.RowSorter
import javax.swing.SortOrder
import javax.swing.table.TableRowSorter
import kotlin.Any
import kotlin.Comparator
import kotlin.Exception
import kotlin.arrayOf
import kotlin.synchronized
import org.micoli.php.symfony.list.DoctrineEntityElementDTO
import org.micoli.php.symfony.list.DoctrineEntityService
import org.micoli.php.ui.table.AbstractListPanel
import org.micoli.php.ui.table.ActionIconRenderer
import org.micoli.php.ui.table.ObjectTableModel

class DoctrineEntitiesPanel(project: Project) :
    AbstractListPanel<DoctrineEntityElementDTO>(
        project, "doctrineEntities", arrayOf("Entity", "Table", "Schema", "Action")) {
    override fun getSorter(): TableRowSorter<ObjectTableModel<DoctrineEntityElementDTO>> {
        innerSorter = TableRowSorter<ObjectTableModel<DoctrineEntityElementDTO>>(model)
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
        table.getColumnModel().getColumn(0).setMaxWidth(600)
        table.getColumnModel().getColumn(1).setMaxWidth(200)
        table.getColumnModel().getColumn(2).setMaxWidth(100)
        table.getColumnModel().getColumn(3).setCellRenderer(ActionIconRenderer())
        table.getColumnModel().getColumn(3).setMinWidth(50)
        table.getColumnModel().getColumn(3).setMaxWidth(50)
    }

    override fun handleActionDoubleClick(elementDTO: DoctrineEntityElementDTO) {
        val navigatable = elementDTO.element as? Navigatable ?: return

        ApplicationManager.getApplication().executeOnPooledThread { navigatable.navigate(true) }
    }

    override fun refresh() {
        ApplicationManager.getApplication().executeOnPooledThread {
            synchronized(lock) {
                try {
                    table.emptyText.text = "Loading Entities, please wait..."
                    clearItems()
                    ApplicationManager.getApplication().runReadAction {
                        for (item in DoctrineEntityService.getInstance(project).getElements()) {
                            model.addRow(
                                item, arrayOf(item.className, item.name, item.schema, null))
                        }
                        table.emptyText.text = "Nothing to show"
                        model.fireTableDataChanged()
                    }
                } catch (e: Exception) {
                    logger.error("Error refreshing Entities table", e)
                }
            }
        }
    }
}
