package org.micoli.php.service.intellij

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager

class Editors {
    fun openFileInEditor(project: Project, path: String, line: Int) {
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
