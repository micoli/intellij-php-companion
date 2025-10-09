package org.micoli.php.ui.panels.symfonyProfiler

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.ui.table.JBTable
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import org.micoli.php.symfony.profiler.models.BackTrace
import org.micoli.php.ui.table.ObjectTableModel

class BackTraceTable(val project: Project, backtraces: List<BackTrace>) : JBTable() {
    init {
        val model = object : ObjectTableModel<BackTrace>(arrayOf("File")) {}
        setModel(model)
        setShowColumns(true)
        setShowGrid(true)
        isStriped = true
        autoResizeMode = AUTO_RESIZE_LAST_COLUMN
        for (backtrace in backtraces) {
            model.addRow(backtrace, arrayOf(backtrace.file))
        }
        addMouseListener(
            object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    val row = rowAtPoint(e.getPoint())
                    if (e.clickCount == 2 && row >= 0) {
                        val objectAt = model.getObjectAt(row)
                        openFileInEditor(objectAt.file, objectAt.line.toInt())
                        return
                    }
                }
            })
    }

    private fun openFileInEditor(path: String, line: Int) {
        val file = VirtualFileManager.getInstance().findFileByUrl("file://$path") ?: return

        val fileEditorManager = FileEditorManager.getInstance(project)
        fileEditorManager.openFile(file, true).firstOrNull()?.let { editor ->
            if (editor is com.intellij.openapi.fileEditor.TextEditor) {
                val textEditor = editor.editor
                val lineStartOffset = textEditor.document.getLineStartOffset(line - 1)
                textEditor.caretModel.moveToOffset(lineStartOffset)
                textEditor.scrollingModel.scrollToCaret(
                    com.intellij.openapi.editor.ScrollType.CENTER)
            }
        }
    }
}
