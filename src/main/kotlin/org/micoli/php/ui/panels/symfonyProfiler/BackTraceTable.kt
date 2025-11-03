package org.micoli.php.ui.panels.symfonyProfiler

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.setEmptyState
import com.intellij.ui.JBColor
import com.intellij.ui.table.JBTable
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import org.micoli.php.service.filesystem.PathUtil
import org.micoli.php.service.intellij.Editors
import org.micoli.php.symfony.profiler.htmlParsers.FileLocation
import org.micoli.php.ui.table.ColoredCellRenderer
import org.micoli.php.ui.table.ObjectTableModel

class BackTraceTable(val project: Project, backtraces: List<FileLocation>) : JBTable() {
    init {
        val model = object : ObjectTableModel<FileLocation>(arrayOf("Backtrace")) {}
        setModel(model)
        setEmptyState(
            "Nothing to show, may be you need to add 'profiling_collect_backtrace: true' to doctrine.yaml")
        setShowColumns(true)
        setShowGrid(true)
        columnModel.getColumn(0).apply {
            cellRenderer = ColoredCellRenderer {
                if (it.contains("vendor/")) {
                    JBColor.ORANGE
                } else {
                    null
                }
            }
        }

        isStriped = true
        autoResizeMode = AUTO_RESIZE_LAST_COLUMN
        val baseDir = PathUtil.getBaseDir(project)?.canonicalPath ?: ""
        model.setRows(backtraces) { arrayOf(it.file.replaceFirst(baseDir, "")) }
        addMouseListener(
            object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    val row = rowAtPoint(e.getPoint())
                    val col = columnAtPoint(e.getPoint())
                    if (e.clickCount == 2 && row >= 0) {
                        val objectAt = model.getObjectAt(row) ?: return
                        Editors().openFileInEditor(project, objectAt.file, objectAt.line ?: 0)
                        return
                    }
                }
            })
    }
}
