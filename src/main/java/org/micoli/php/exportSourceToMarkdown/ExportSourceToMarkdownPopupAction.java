package org.micoli.php.exportSourceToMarkdown;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.micoli.php.service.ParsedContentDisplayPopup;
import org.micoli.php.ui.Notification;

public class ExportSourceToMarkdownPopupAction extends AbstractExportSourceToMarkdownAction {
    @Override
    protected void doAction(Project project, VirtualFile[] selectedFiles) {
        ExportedSource export = ExportSourceToMarkdownService.generateMarkdownExport(project, selectedFiles);
        if (export == null) {
            Notification.error("No files found for export.");
            return;
        }
        ParsedContentDisplayPopup.showMarkdownPopup(project, export.content());
        Notification.messageWithTimeout(String.format("Approximatively number of tokens: %s", export.numberOfTokens()), 500);
    }
}
