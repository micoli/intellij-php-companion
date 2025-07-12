package org.micoli.php.exportSourceToMarkdown;

import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.micoli.php.ui.Notification;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

public class ExportSourceToMarkdownClipboardAction extends AbstractExportSourceToMarkdownAction {
    @Override
    protected void doAction(Project project, VirtualFile[] selectedFiles) {
        String content = ExportSourceToMarkdownService.generateMarkdownExport(project, selectedFiles);
        if (content == null) {
            Notification.error("No files found for export.");
            return;
        }

        copyToClipboard(content);
        Notification.messageWithTimeout("Content copied to clipboard", 500);
    }

    private static void copyToClipboard(String content) {
        CopyPasteManager.getInstance().setContents(new Transferable() {
            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[] { DataFlavor.stringFlavor, DataFlavor.getTextPlainUnicodeFlavor() };
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return DataFlavor.stringFlavor.equals(flavor) || DataFlavor.getTextPlainUnicodeFlavor().equals(flavor);
            }

            @Override
            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
                if (DataFlavor.stringFlavor.equals(flavor)) {
                    return content;
                }
                else if (DataFlavor.getTextPlainUnicodeFlavor().equals(flavor)) {
                    return content;
                }
                throw new UnsupportedFlavorException(flavor);
            }
        });
    }
}
