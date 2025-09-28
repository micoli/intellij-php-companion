package org.micoli.php.ui.panels

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.pom.Navigatable
import java.awt.Component
import java.lang.Short
import java.lang.String
import javax.swing.*
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableRowSorter
import kotlin.Any
import kotlin.Boolean
import kotlin.Comparator
import kotlin.Exception
import kotlin.Int
import kotlin.arrayOf
import kotlin.synchronized
import kotlin.text.trimIndent
import org.micoli.php.symfony.list.RouteElementDTO
import org.micoli.php.symfony.list.RouteService

class RoutesPanel(project: Project) :
    AbstractListPanel<RouteElementDTO?>(project, "routes", COLUMN_NAMES) {
    override fun getSorter(): TableRowSorter<DefaultTableModel> {
        innerSorter = TableRowSorter<DefaultTableModel>(model)
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
        table.getColumnModel()?.getColumn(0)?.setMaxWidth(1600)
        table.getColumnModel()?.getColumn(1)?.setMaxWidth(90)
        table.getColumnModel()?.getColumn(2)?.setCellRenderer(ActionIconRenderer())
        table.getColumnModel()?.getColumn(2)?.setMinWidth(50)
        table.getColumnModel()?.getColumn(2)?.setMaxWidth(50)
        val baseRowHeight = table.getRowHeight()
        table.setRowHeight(baseRowHeight.times(2))
        table
            .getColumnModel()
            .getColumn(0)
            .setCellRenderer(
                object : DefaultTableCellRenderer() {
                    private val jLabel = JLabel()

                    override fun getTableCellRendererComponent(
                        table: JTable,
                        value: Any?,
                        isSelected: Boolean,
                        hasFocus: Boolean,
                        row: Int,
                        column: Int,
                    ): Component {
                        val elementDTO = value as RouteElementDTO?
                        jLabel.setText(
                            if (value == null) ""
                            else
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
                                    elementDTO!!.uri,
                                    elementDTO.fqcn,
                                ))

                        jLabel.setBackground(
                            if (isSelected) table.getSelectionBackground()
                            else table.getBackground())
                        jLabel.setForeground(
                            if (isSelected) table.getSelectionForeground()
                            else table.getForeground())
                        jLabel.setSize(
                            table.getColumnModel().getColumn(column).getWidth(),
                            Short.MAX_VALUE.toInt())

                        return jLabel
                    }
                })
    }

    override fun handleActionClick(row: Int) {
        ApplicationManager.getApplication().invokeLater {
            val elementDTO = table.getValueAt(row, getColumnCount() - 1) as RouteElementDTO
            (elementDTO.element as? Navigatable)?.navigate(true)
        }
    }

    override fun refresh() {
        synchronized(lock) {
            try {
                table.emptyText.text = "Loading routes, please wait..."
                clearItems()

                val worker: SwingWorker<Void?, RouteElementDTO> =
                    object : SwingWorker<Void?, RouteElementDTO>() {
                        override fun doInBackground(): Void? {
                            ApplicationManager.getApplication().runReadAction {
                                val routeListService = RouteService.getInstance(project)
                                for (item in routeListService.getElements()) {
                                    publish(item)
                                }
                            }
                            return null
                        }

                        override fun process(chunks: MutableList<RouteElementDTO>) {
                            SwingUtilities.invokeLater {
                                for (item in chunks) {
                                    model.addRow(arrayOf<Any?>(item, item.methods, item))
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
                LOGGER.error("Error refreshing routes table", e)
            }
        }
    }

    override fun getColumnCount(): Int {
        return COLUMN_NAMES.size
    }

    companion object {
        private val COLUMN_NAMES = arrayOf("URI", "Method", "Actions")
    }
}
