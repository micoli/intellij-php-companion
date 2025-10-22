package org.micoli.php.exportSourceToMarkdown.actions

import com.intellij.lang.Language
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.micoli.php.exportSourceToMarkdown.ExportSourceToMarkdownService
import org.micoli.php.exportSourceToMarkdown.ExportedSource
import org.micoli.php.service.intellij.ScratchFileUtil.createAndOpenScratchFile
import org.micoli.php.ui.Notification

class ExportSourceToMarkdownScratchFileAction : AbstractExportSourceToMarkdownAction() {
    override fun doAction(project: Project, selectedFiles: Array<VirtualFile>) {
        val export: ExportedSource? =
            ExportSourceToMarkdownService.getInstance(project).generateMarkdownExport(selectedFiles)
        if (export == null) {
            Notification.getInstance(project).error("No files found for export.")
            return
        }
        createAndOpenScratchFile(
            project, "exportedSource", Language.findLanguageByID("Markdown"), export.content ?: "")
        Notification.getInstance(project)
            .messageWithTimeout(
                String.format("Approximatively number of tokens: %s", export.numberOfTokens), 500)
    }
}
