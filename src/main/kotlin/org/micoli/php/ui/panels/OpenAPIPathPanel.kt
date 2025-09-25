package org.micoli.php.ui.panels

import com.intellij.find.FindModel
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.usageView.UsageInfo
import com.intellij.usages.UsageInfo2UsageAdapter
import java.awt.Component
import java.lang.Short
import java.lang.String
import java.time.Duration
import java.util.function.Consumer
import java.util.function.Function
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
import org.micoli.php.openAPI.OpenAPIPathElementDTO
import org.micoli.php.openAPI.OpenAPIService
import org.micoli.php.service.intellij.psi.PsiElementUtil
import org.micoli.php.service.intellij.search.ConcurrentSearchManager
import org.micoli.php.service.intellij.search.SearchWithCompletionIndicator
import org.micoli.php.ui.Notification
import org.micoli.php.ui.popup.NavigableItem

class OpenAPIPathPanel(project: Project) :
    AbstractListPanel<OpenAPIPathElementDTO?>(project, "openAPI", COLUMN_NAMES) {
    var concurrentSearchManager: ConcurrentSearchManager =
        ConcurrentSearchManager(Duration.ofSeconds(20))

    override fun getSorter(): TableRowSorter<DefaultTableModel>? {
        innerSorter = TableRowSorter<DefaultTableModel>(model)
        innerSorter?.setSortKeys(
            listOf<RowSorter.SortKey?>(
                RowSorter.SortKey(0, SortOrder.ASCENDING),
                RowSorter.SortKey(1, SortOrder.ASCENDING),
            ))
        innerSorter?.setComparator(
            0,
            Comparator { o1: OpenAPIPathElementDTO?, o2: OpenAPIPathElementDTO? ->
                String.CASE_INSENSITIVE_ORDER.compare(o1!!.uri, o2!!.uri)
            },
        )
        innerSorter?.setComparator(1, String.CASE_INSENSITIVE_ORDER)
        innerSorter?.setComparator(2, Comparator { _: Any?, _: Any? -> 0 })
        return innerSorter
    }

    override fun configureTableColumns() {
        table?.getColumnModel()?.getColumn(0)?.setMaxWidth(1600)
        table?.getColumnModel()?.getColumn(1)?.setMaxWidth(90)
        table?.getColumnModel()?.getColumn(2)?.setCellRenderer(ActionIconRenderer())
        table?.getColumnModel()?.getColumn(2)?.setMinWidth(50)
        table?.getColumnModel()?.getColumn(2)?.setMaxWidth(50)
        table?.setRowHeight(table?.getRowHeight()?.times(2) ?: 20)
        table
            ?.getColumnModel()
            ?.getColumn(0)
            ?.setCellRenderer(
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
                        val elementDTO = value as OpenAPIPathElementDTO?
                        jLabel.setText(
                            if (value != null)
                                String.format(
                                    """
                                        <html>
                                            <div>
                                                %s<br>
                                                <small color="#777">%s <strong>(%s)</strong></small>
                                            </div>
                                        </html>
                                        
                                        """
                                        .trimIndent(),
                                    elementDTO!!.uri,
                                    elementDTO.description,
                                    elementDTO.operationId,
                                )
                            else "")

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
            val elementDTO =
                table?.getValueAt(row, getColumnCount() - 1) as OpenAPIPathElementDTO?
                    ?: return@invokeLater
            searchOperationIdDeclaration("operationId: " + elementDTO.operationId)
        }
    }

    override fun refresh() {
        synchronized(lock) {
            try {
                table?.emptyText?.text = "Loading OpenAPIPaths, please wait..."
                clearItems()

                val worker: SwingWorker<Void?, OpenAPIPathElementDTO> =
                    object : SwingWorker<Void?, OpenAPIPathElementDTO>() {
                        override fun doInBackground(): Void? {
                            ApplicationManager.getApplication().runReadAction {
                                val items =
                                    OpenAPIService.getInstance(project).getElements()
                                        ?: return@runReadAction
                                for (item in items) {
                                    publish(item)
                                }
                            }
                            return null
                        }

                        override fun process(chunks: MutableList<OpenAPIPathElementDTO>) {
                            SwingUtilities.invokeLater {
                                for (item in chunks) {
                                    model.addRow(arrayOf<Any?>(item, item.method, item))
                                }
                            }
                        }

                        override fun done() {
                            SwingUtilities.invokeLater {
                                table?.emptyText?.text = "Nothing to show"
                                model.fireTableDataChanged()
                            }
                        }
                    }
                worker.execute()
            } catch (e: Exception) {
                LOGGER.error("Error refreshing OpenAPIPaths table", e)
            }
        }
    }

    private fun searchOperationIdDeclaration(searchText: kotlin.String) {
        if (concurrentSearchManager.isSearchInProgress(searchText)) {
            Notification.getInstance(project).messageWithTimeout("Search already in progress", 1000)
            return
        }

        ApplicationManager.getApplication().invokeLater {
            concurrentSearchManager.addSearch(searchText)
            val findModel = FindModel()

            findModel.isGlobal = true
            findModel.isProjectScope = true
            findModel.isRegularExpressions = true
            findModel.isWithSubdirectories = true
            findModel.setFileFilter(null)
            findModel.stringToFind = searchText
            findModel.setFileFilter("*.yaml,*.yml")
            SearchWithCompletionIndicator.findUsagesWithProgress(
                findModel,
                project,
                1500,
                Consumer { results: MutableList<UsageInfo>? ->
                    concurrentSearchManager.removeSearch(searchText)
                    if (results == null || results.isEmpty()) {
                        Notification.getInstance(project)
                            .messageWithTimeout("No OperationId found", 1500)
                        return@Consumer
                    }
                    results
                        .stream()
                        .map(
                            Function { usageInfo: UsageInfo? ->
                                ApplicationManager.getApplication()
                                    .runReadAction<NavigableItem?>(
                                        Computable {
                                            usageInfo!!.file ?: return@Computable null

                                            val fileExtract =
                                                PsiElementUtil.getFileExtract(usageInfo, 1)
                                            NavigableItem(
                                                fileExtract,
                                                UsageInfo2UsageAdapter(usageInfo),
                                                usageInfo.icon)
                                        })
                            })
                        .toList()
                        .forEach(Consumer { c: NavigableItem? -> c!!.navigate(true) })
                },
            )
        }
    }

    override fun getColumnCount(): Int {
        return COLUMN_NAMES.size
    }

    companion object {
        private val COLUMN_NAMES = arrayOf<kotlin.String?>("Uri", "Method", "Actions")
    }
}
