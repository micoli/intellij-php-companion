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
import org.micoli.php.symfony.list.CommandElementDTO
import org.micoli.php.symfony.list.CommandService
import org.micoli.php.ui.table.AbstractListPanel
import org.micoli.php.ui.table.ActionIconRenderer
import org.micoli.php.ui.table.ObjectTableModel

class CommandsPanel(project: Project) :
    AbstractListPanel<CommandElementDTO>(
        project, "commands", arrayOf("Command", "Description", "Action")) {
    override fun getSorter(): TableRowSorter<ObjectTableModel<CommandElementDTO>> {
        val innerSorter = TableRowSorter(model)
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
        table.columnModel.apply {
            getColumn(0).setMaxWidth(800)
            getColumn(1).setMaxWidth(200)
            getColumn(2).setCellRenderer(ActionIconRenderer())
            getColumn(2).setMinWidth(50)
            getColumn(2).setMaxWidth(50)
        }
    }

    override fun handleActionDoubleClick(elementDTO: CommandElementDTO): Boolean {
        val navigatable = elementDTO.element as? Navigatable ?: return false
        ApplicationManager.getApplication().runReadAction({ navigatable.navigate(true) })

        return true
    }

    override fun setElements() {
        try {
            table.emptyText.text = "Loading CLI, please wait..."
            val elements: MutableList<CommandElementDTO> =
                ApplicationManager.getApplication()
                    .runReadAction(Computable { CommandService.getInstance(project).getElements() })
            model.setRows(elements) { arrayOf(it.command, it.description, null) }
        } catch (e: Exception) {
            logger.error("Error refreshing CLI table", e)
        }
    }
}
