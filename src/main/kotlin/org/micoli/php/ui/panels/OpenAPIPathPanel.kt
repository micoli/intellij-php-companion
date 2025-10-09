package org.micoli.php.ui.panels

import com.intellij.find.FindModel
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.usageView.UsageInfo
import com.intellij.usages.UsageInfo2UsageAdapter
import java.lang.String
import java.time.Duration
import javax.swing.*
import javax.swing.table.TableRowSorter
import kotlin.Any
import kotlin.Comparator
import kotlin.Exception
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
import org.micoli.php.ui.table.AbstractListPanel
import org.micoli.php.ui.table.ActionIconRenderer
import org.micoli.php.ui.table.CustomCellRenderer
import org.micoli.php.ui.table.ObjectTableModel

class OpenAPIPathPanel(project: Project) :
    AbstractListPanel<OpenAPIPathElementDTO>(
        project, "openAPI", arrayOf("Uri", "Method", "Action")) {
    var concurrentSearchManager: ConcurrentSearchManager =
        ConcurrentSearchManager(Duration.ofSeconds(20))

    override fun getSorter(): TableRowSorter<ObjectTableModel<OpenAPIPathElementDTO>> {
        innerSorter = TableRowSorter<ObjectTableModel<OpenAPIPathElementDTO>>(model)
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
        table.getColumnModel().getColumn(0).setMaxWidth(1600)
        table.getColumnModel().getColumn(1).setMaxWidth(90)
        table.getColumnModel().getColumn(2).setCellRenderer(ActionIconRenderer())
        table.getColumnModel().getColumn(2).setMinWidth(50)
        table.getColumnModel().getColumn(2).setMaxWidth(50)
        table.setRowHeight(table.getRowHeight().times(2))
        table
            .getColumnModel()
            .getColumn(0)
            .setCellRenderer(
                CustomCellRenderer<OpenAPIPathElementDTO> {
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
                        it.uri,
                        it.description,
                        it.operationId,
                    )
                })
    }

    override fun handleActionDoubleClick(elementDTO: OpenAPIPathElementDTO) {
        ApplicationManager.getApplication().executeOnPooledThread {
            searchOperationIdDeclaration("operationId: " + elementDTO.operationId)
        }
    }

    override fun refresh() {
        ApplicationManager.getApplication().executeOnPooledThread {
            synchronized(lock) {
                try {
                    table.emptyText.text = "Loading OpenAPIPaths, please wait..."
                    clearItems()
                    ApplicationManager.getApplication().runReadAction {
                        for (item in OpenAPIService.getInstance(project).getElements()) {
                            model.addRow(item, arrayOf(item.uri, item.method, null))
                        }
                        table.emptyText.text = "Nothing to show"
                        model.fireTableDataChanged()
                    }
                } catch (e: Exception) {
                    logger.error("Error refreshing OpenAPIPaths table", e)
                }
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
            findModel.fileFilter = null
            findModel.stringToFind = searchText
            findModel.fileFilter = "*.yaml,*.yml"
            SearchWithCompletionIndicator.findUsagesWithProgress(
                findModel,
                project,
                1500,
            ) { results: MutableList<UsageInfo>? ->
                concurrentSearchManager.removeSearch(searchText)
                if (results == null || results.isEmpty()) {
                    Notification.getInstance(project)
                        .messageWithTimeout("No OperationId found", 1500)
                    return@findUsagesWithProgress
                }
                results
                    .stream()
                    .map {
                        ApplicationManager.getApplication().runReadAction<NavigableItem?> {
                            it!!.file ?: return@runReadAction null

                            val fileExtract = PsiElementUtil.getFileExtract(it, 1)
                            NavigableItem(fileExtract, UsageInfo2UsageAdapter(it), it.icon)
                        }
                    }
                    .toList()
                    .forEach { it!!.navigate(true) }
            }
        }
    }
}
