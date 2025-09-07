package org.micoli.php.exportSourceToMarkdown.actions;

import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.micoli.php.exportSourceToMarkdown.ExportSourceToMarkdownService;
import org.micoli.php.exportSourceToMarkdown.ExportedSource;
import org.micoli.php.service.intellij.ScratchFileUtil;
import org.micoli.php.ui.Notification;

public class ExportSourceToMarkdownScratchFileAction extends AbstractExportSourceToMarkdownAction {
    @Override
    protected void doAction(Project project, VirtualFile[] selectedFiles) {
        ExportedSource export =
                ExportSourceToMarkdownService.getInstance(project).generateMarkdownExport(selectedFiles);
        if (export == null) {
            Notification.error("No files found for export.");
            return;
        }
        ScratchFileUtil.createAndOpenScratchFile(
                project, "exportedSource", Language.findLanguageByID("Markdown"), export.content());
        Notification.messageWithTimeout(
                String.format("Approximatively number of tokens: %s", export.numberOfTokens()), 500);
    }
}
