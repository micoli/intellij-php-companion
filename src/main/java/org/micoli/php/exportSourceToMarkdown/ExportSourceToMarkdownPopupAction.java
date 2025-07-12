package org.micoli.php.exportSourceToMarkdown;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.micoli.php.service.ParsedContentDisplayPopup;
import org.micoli.php.ui.Notification;

public class ExportSourceToMarkdownPopupAction extends AbstractExportSourceToMarkdownAction {
    @Override
    protected void doAction(Project project, VirtualFile[] selectedFiles) {
        String content = ExportSourceToMarkdownService.generateMarkdownExport(project, selectedFiles);
        if (content == null) {
            Notification.error("No files found for export.");
            return;
        }
        ParsedContentDisplayPopup.showMarkdownPopup(project, content);
    }
}
