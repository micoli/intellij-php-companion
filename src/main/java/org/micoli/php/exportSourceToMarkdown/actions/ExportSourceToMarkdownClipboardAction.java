package org.micoli.php.exportSourceToMarkdown.actions;

import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import org.jetbrains.annotations.NotNull;
import org.micoli.php.exportSourceToMarkdown.ExportSourceToMarkdownService;
import org.micoli.php.exportSourceToMarkdown.ExportedSource;
import org.micoli.php.ui.Notification;

public class ExportSourceToMarkdownClipboardAction extends AbstractExportSourceToMarkdownAction {
    @Override
    protected void doAction(Project project, VirtualFile[] selectedFiles) {
        ExportedSource export =
                ExportSourceToMarkdownService.getInstance(project).generateMarkdownExport(project, selectedFiles);
        if (export == null) {
            Notification.error("No files found for export.");
            return;
        }

        copyToClipboard(export.content());
        Notification.messageWithTimeout(
                String.format(
                        "Content copied to clipboard, approximatively number of tokens: %s", export.numberOfTokens()),
                500);
    }

    private static void copyToClipboard(String content) {
        CopyPasteManager.getInstance().setContents(new Transferable() {
            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[] {DataFlavor.stringFlavor, DataFlavor.getTextPlainUnicodeFlavor()};
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return DataFlavor.stringFlavor.equals(flavor)
                        || DataFlavor.getTextPlainUnicodeFlavor().equals(flavor);
            }

            @Override
            public @NotNull Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
                if (DataFlavor.stringFlavor.equals(flavor)) {
                    return content;
                } else if (DataFlavor.getTextPlainUnicodeFlavor().equals(flavor)) {
                    return content;
                }
                throw new UnsupportedFlavorException(flavor);
            }
        });
    }
}
