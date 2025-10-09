package org.micoli.php.ui.panels.symfonyProfiler

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.ui.table.JBTable
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import org.micoli.php.service.filesystem.PathUtil
import org.micoli.php.symfony.profiler.parsers.FileLocation
import org.micoli.php.ui.table.ObjectTableModel

class BackTraceTable(val project: Project, backtraces: List<FileLocation>) : JBTable() {
    init {
        val model = object : ObjectTableModel<FileLocation>(arrayOf("File")) {}
        setModel(model)
        setShowColumns(true)
        setShowGrid(true)
        isStriped = true
        autoResizeMode = AUTO_RESIZE_LAST_COLUMN
        val baseDir = PathUtil.getBaseDir(project)?.canonicalPath ?: ""
        for (backtrace in backtraces) {
            model.addRow(backtrace, arrayOf(backtrace.file.replaceFirst(baseDir, "")))
        }
        addMouseListener(
            object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    val row = rowAtPoint(e.getPoint())
                    if (e.clickCount == 2 && row >= 0) {
                        val objectAt = model.getObjectAt(row) ?: return
                        openFileInEditor(objectAt.file, objectAt.line ?: 0)
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
