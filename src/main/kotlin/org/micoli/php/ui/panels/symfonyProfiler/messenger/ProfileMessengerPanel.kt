package org.micoli.php.ui.panels.symfonyProfiler.messenger

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import java.awt.BorderLayout
import javax.swing.JTable
import javax.swing.RowSorter
import javax.swing.SortOrder
import javax.swing.table.TableRowSorter
import kotlin.arrayOf
import org.micoli.php.service.filesystem.PathUtil
import org.micoli.php.service.intellij.Editors
import org.micoli.php.service.intellij.psi.PhpUtil.findClassByFQN
import org.micoli.php.service.intellij.psi.PhpUtil.normalizeNonRootFQN
import org.micoli.php.symfony.profiler.SymfonyProfileService
import org.micoli.php.symfony.profiler.parsers.MessengerData
import org.micoli.php.symfony.profiler.parsers.MessengerDispatch
import org.micoli.php.ui.panels.symfonyProfiler.AbstractProfilePanel
import org.micoli.php.ui.table.AbstractListPanel
import org.micoli.php.ui.table.CustomCellRenderer
import org.micoli.php.ui.table.ObjectTableModel

class ProfileMessengerPanel(project: Project) : AbstractProfilePanel(project) {
    val baseDir: String = PathUtil.getBaseDir(project)?.canonicalPath ?: ""

    val table =
        object :
            AbstractListPanel<MessengerDispatch>(
                project, "dispatches", arrayOf("Bus", "Message", "Dispatch")) {
            override fun getSorter(): TableRowSorter<ObjectTableModel<MessengerDispatch>> {
                val innerSorter = TableRowSorter(model)
                innerSorter.setSortKeys(
                    listOf<RowSorter.SortKey?>(
                        RowSorter.SortKey(0, SortOrder.ASCENDING),
                    ))
                innerSorter.setComparator(1, java.lang.String.CASE_INSENSITIVE_ORDER)
                innerSorter.setComparator(2, java.lang.String.CASE_INSENSITIVE_ORDER)
                return innerSorter
            }

            override fun configureTableColumns() {
                table.apply {
                    setShowColumns(true)
                    setShowGrid(true)
                    isStriped = true

                    columnModel.getColumn(0).apply {
                        preferredWidth = 100
                        maxWidth = 100
                        minWidth = 100
                    }
                    columnModel.getColumn(1).apply {
                        preferredWidth = 120
                        minWidth = 100
                    }
                    columnModel.getColumn(2).apply {
                        minWidth = 100
                        cellRenderer =
                            CustomCellRenderer<MessengerDispatch> {
                                String.format(
                                    "%s:%s",
                                    it.messageLocation?.file?.replaceFirst(baseDir, ""),
                                    it.messageLocation?.line,
                                )
                            }
                    }

                    autoResizeMode = JTable.AUTO_RESIZE_LAST_COLUMN
                }
            }

            override fun refresh() {
                ApplicationManager.getApplication().executeOnPooledThread {
                    SymfonyProfileService.getInstance(project)
                        .loadProfilerDumpPage(
                            MessengerData::class.java,
                            symfonyProfileDTO.token,
                            loaderLogCallback(System.nanoTime()),
                            { showError(it) },
                            {
                                synchronized(lock) {
                                    while (model.rowCount > 0) {
                                        model.removeRow(0)
                                    }
                                    for (dispatch in
                                        it?.dispatches ?: return@loadProfilerDumpPage) {
                                        model.addRow(
                                            dispatch,
                                            arrayOf(
                                                dispatch.busName,
                                                dispatch.messageName,
                                                dispatch.dispatch))
                                    }
                                    showMainPanel()
                                }
                            })
                }
            }

            override fun handleActionDoubleClick(col: Int, elementDTO: MessengerDispatch): Boolean {
                return when (col) {
                    1 -> {
                        val phpClass =
                            findClassByFQN(project, normalizeNonRootFQN(elementDTO.messageName))
                                ?: return false
                        val virtualFile = phpClass.containingFile.virtualFile
                        if (virtualFile != null) {
                            FileEditorManager.getInstance(project).openFile(virtualFile, true)
                        }
                        true
                    }

                    2 -> {
                        if (elementDTO.dispatch == null) {
                            return false
                        }
                        Editors()
                            .openFileInEditor(
                                project, elementDTO.dispatch.file, elementDTO.dispatch.line ?: 0)
                        true
                    }

                    else -> false
                }
            }
        }

    init {
        mainPanel.add(table, BorderLayout.CENTER)
        initialize()
    }

    override fun refresh() {
        table.refresh()
    }
}
