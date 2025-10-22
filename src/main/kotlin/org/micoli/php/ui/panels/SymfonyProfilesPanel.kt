package org.micoli.php.ui.panels

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import java.lang.String
import javax.swing.RowSorter
import javax.swing.SortOrder
import javax.swing.table.TableRowSorter
import org.micoli.php.symfony.profiler.SymfonyProfileDTO
import org.micoli.php.symfony.profiler.SymfonyProfileService
import org.micoli.php.ui.SymfonyWindowContent
import org.micoli.php.ui.table.AbstractListPanel
import org.micoli.php.ui.table.ActionIconRenderer
import org.micoli.php.ui.table.ObjectTableModel
import org.micoli.php.ui.table.TimestampRenderer

class SymfonyProfilesPanel(project: Project, val symfonyWindowContent: SymfonyWindowContent) :
    AbstractListPanel<SymfonyProfileDTO>(
        project, "symfonyProfiles", arrayOf("Timestamp", "Method", "URI", "Code", "Action")) {
    val symfonyProfileService: SymfonyProfileService = SymfonyProfileService.getInstance(project)
    var isAutoRefresh: Boolean = false
        set(value) {
            symfonyProfileService.setAutoRefresh(value)
        }

    init {
        symfonyProfileService.setAutoRefresh(isAutoRefresh)
    }

    override fun getSorter(): TableRowSorter<ObjectTableModel<SymfonyProfileDTO>> {
        val innerSorter = TableRowSorter(model)
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
        table.columnModel.apply {
            getColumn(0).setCellRenderer(TimestampRenderer())
            getColumn(0).setMaxWidth(85)
            getColumn(1).setMaxWidth(60)
            getColumn(2).setMaxWidth(800)
            getColumn(3).setMaxWidth(60)
            getColumn(4).setCellRenderer(ActionIconRenderer())
            getColumn(4).setMinWidth(50)
            getColumn(4).setMaxWidth(50)
        }
    }

    override fun handleActionDoubleClick(elementDTO: SymfonyProfileDTO): Boolean {
        ApplicationManager.getApplication().executeOnPooledThread {
            BrowserUtil.open(elementDTO.profileUrl)
        }
        return true
    }

    override fun handleActionLineSelected(elementDTO: SymfonyProfileDTO): Boolean {
        symfonyWindowContent.profileSelected(elementDTO)
        return true
    }

    override fun setElements() {
        val urlRoots = SymfonyProfileService.getInstance(project).configuration?.urlRoots
        try {
            table.emptyText.text = "Loading profiles, please wait..."
            val elements: MutableList<SymfonyProfileDTO> =
                ApplicationManager.getApplication()
                    .runReadAction(
                        Computable { SymfonyProfileService.getInstance(project).elements })
            model.setRows(elements) {
                arrayOf(
                    it.timestamp,
                    it.method,
                    urlRoots?.fold(it.url) { acc, root -> acc.replaceFirst(root, "") } ?: it.url,
                    it.statusCode,
                    null)
            }
        } catch (e: Exception) {
            logger.error("Error refreshing profilers " + e.localizedMessage, e)
        }
    }
}
