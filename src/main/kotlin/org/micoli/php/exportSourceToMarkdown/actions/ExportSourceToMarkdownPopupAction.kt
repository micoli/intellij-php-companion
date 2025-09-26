package org.micoli.php.exportSourceToMarkdown.actions

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.micoli.php.exportSourceToMarkdown.ExportSourceToMarkdownService
import org.micoli.php.exportSourceToMarkdown.ExportedSource
import org.micoli.php.ui.Notification
import org.micoli.php.ui.popup.ParsedContentDisplayPopup.Companion.showMarkdownPopup

class ExportSourceToMarkdownPopupAction : AbstractExportSourceToMarkdownAction() {
    override fun doAction(project: Project, selectedFiles: Array<VirtualFile>) {
        val export: ExportedSource? =
            ExportSourceToMarkdownService.getInstance(project).generateMarkdownExport(selectedFiles)
        if (export == null) {
            Notification.getInstance(project).error("No files found for export.")
            return
        }
        showMarkdownPopup(project, export.content ?: "")
        Notification.getInstance(project)
            .messageWithTimeout(
                String.format("Approximatively number of tokens: %s", export.numberOfTokens), 500)
    }
}
