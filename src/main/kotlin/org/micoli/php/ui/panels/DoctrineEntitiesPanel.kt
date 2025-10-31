package org.micoli.php.ui.panels

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.pom.Navigatable
import java.lang.String
import javax.swing.RowSorter
import javax.swing.SortOrder
import javax.swing.table.TableRowSorter
import kotlin.Any
import kotlin.Comparator
import kotlin.Exception
import kotlin.arrayOf
import org.micoli.php.symfony.list.DoctrineEntityElementDTO
import org.micoli.php.symfony.list.DoctrineEntityService
import org.micoli.php.ui.table.AbstractListPanel
import org.micoli.php.ui.table.ActionIconRenderer
import org.micoli.php.ui.table.ObjectTableModel

class DoctrineEntitiesPanel(project: Project) :
    AbstractListPanel<DoctrineEntityElementDTO>(
        project, "doctrineEntities", arrayOf("Entity", "Table", "Schema", "Action")) {
    override fun getSorter(): TableRowSorter<ObjectTableModel<DoctrineEntityElementDTO>> {
        val innerSorter = TableRowSorter(model)
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
        table.columnModel.apply {
            getColumn(0).setMaxWidth(600)
            getColumn(1).setMaxWidth(200)
            getColumn(2).setMaxWidth(100)
            getColumn(3).setCellRenderer(ActionIconRenderer())
            getColumn(3).setMinWidth(50)
            getColumn(3).setMaxWidth(50)
        }
    }

    override fun handleActionDoubleClick(elementDTO: DoctrineEntityElementDTO): Boolean {
        val navigatable = elementDTO.element as? Navigatable ?: return false
        ApplicationManager.getApplication().invokeLater {
            ApplicationManager.getApplication().runReadAction { navigatable.navigate(true) }
        }
        return true
    }

    override fun setElements() {
        try {
            table.emptyText.text = "Loading Entities, please wait..."
            val elements: MutableList<DoctrineEntityElementDTO> =
                ApplicationManager.getApplication()
                    .runReadAction(
                        Computable { DoctrineEntityService.getInstance(project).getElements() })
            model.setRows(elements) { arrayOf(it.className, it.name, it.schema, null) }
        } catch (e: Exception) {
            logger.error("Error refreshing Entities table", e)
        }
    }
}
