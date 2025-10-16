package org.micoli.php.ui.panels

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.pom.Navigatable
import java.lang.String
import javax.swing.*
import javax.swing.table.TableRowSorter
import kotlin.Any
import kotlin.Comparator
import kotlin.Exception
import kotlin.arrayOf
import kotlin.synchronized
import kotlin.text.trimIndent
import org.micoli.php.symfony.list.RouteElementDTO
import org.micoli.php.symfony.list.RouteService
import org.micoli.php.ui.table.AbstractListPanel
import org.micoli.php.ui.table.ActionIconRenderer
import org.micoli.php.ui.table.CustomCellRenderer
import org.micoli.php.ui.table.ObjectTableModel

class RoutesPanel(project: Project) :
    AbstractListPanel<RouteElementDTO>(project, "routes", arrayOf("URI", "Method", "Action")) {
    override fun getSorter(): TableRowSorter<ObjectTableModel<RouteElementDTO>> {
        val innerSorter = TableRowSorter(model)
        innerSorter.setSortKeys(
            listOf<RowSorter.SortKey?>(
                RowSorter.SortKey(0, SortOrder.ASCENDING),
                RowSorter.SortKey(1, SortOrder.ASCENDING),
            ))
        innerSorter.setComparator(
            0,
            Comparator { o1: RouteElementDTO?, o2: RouteElementDTO? ->
                String.CASE_INSENSITIVE_ORDER.compare(o1!!.uri, o2!!.uri)
            },
        )
        innerSorter.setComparator(1, String.CASE_INSENSITIVE_ORDER)
        innerSorter.setComparator(2, Comparator { _: Any?, _: Any? -> 0 })
        return innerSorter
    }

    override fun configureTableColumns() {
        table.columnModel.apply {
            getColumn(0).setMaxWidth(1600)
            getColumn(1).setMaxWidth(90)
            getColumn(2).setCellRenderer(ActionIconRenderer())
            getColumn(2).setMinWidth(50)
            getColumn(2).setMaxWidth(50)
            getColumn(0)
                .setCellRenderer(
                    CustomCellRenderer<RouteElementDTO> {
                        String.format(
                            """
                    <html>
                        <div>
                            %s<br>
                            <small color="#777">%s</small>
                        </div>
                    </html>
                    """
                                .trimIndent(),
                            it.uri,
                            it.fqcn,
                        )
                    })
        }
        table.setRowHeight(table.getRowHeight() * 2)
    }

    override fun handleActionDoubleClick(elementDTO: RouteElementDTO): Boolean {
        val navigatable = elementDTO.element as? Navigatable ?: return true

        ApplicationManager.getApplication().executeOnPooledThread { navigatable.navigate(true) }
        return true
    }

    override fun refresh() {
        ApplicationManager.getApplication().executeOnPooledThread {
            synchronized(lock) {
                try {
                    table.emptyText.text = "Loading routes, please wait..."
                    clearItems()
                    ApplicationManager.getApplication().runReadAction {
                        for (item in RouteService.getInstance(project).getElements()) {
                            model.addRow(item, arrayOf(null, item.methods, null))
                        }
                        table.emptyText.text = "Nothing to show"
                        model.fireTableDataChanged()
                    }
                } catch (e: Exception) {
                    logger.error("Error refreshing routes table", e)
                }
            }
        }
    }
}
