package org.micoli.php.exportSourceToMarkdown.actions

import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import org.micoli.php.exportSourceToMarkdown.ExportSourceToMarkdownService
import org.micoli.php.exportSourceToMarkdown.ExportedSource
import org.micoli.php.ui.Notification

class ExportSourceToMarkdownClipboardAction : AbstractExportSourceToMarkdownAction() {
    override fun doAction(project: Project, selectedFiles: Array<VirtualFile>) {
        val export: ExportedSource? =
            ExportSourceToMarkdownService.getInstance(project).generateMarkdownExport(selectedFiles)
        if (export == null) {
            Notification.getInstance(project).error("No files found for export.")
            return
        }

        copyToClipboard(export.content ?: "")
        Notification.getInstance(project)
            .messageWithTimeout(
                String.format(
                    "Content copied to clipboard, approximatively number of tokens: %s",
                    export.numberOfTokens),
                500)
    }

    private fun copyToClipboard(content: String) {
        CopyPasteManager.getInstance()
            .setContents(
                object : Transferable {
                    override fun getTransferDataFlavors(): Array<DataFlavor?> {
                        return arrayOf(
                            DataFlavor.stringFlavor, DataFlavor.getTextPlainUnicodeFlavor())
                    }

                    override fun isDataFlavorSupported(flavor: DataFlavor?): Boolean {
                        return DataFlavor.stringFlavor.equals(flavor) ||
                            DataFlavor.getTextPlainUnicodeFlavor().equals(flavor)
                    }

                    @Throws(UnsupportedFlavorException::class)
                    override fun getTransferData(flavor: DataFlavor?): Any {
                        if (DataFlavor.stringFlavor.equals(flavor)) {
                            return content
                        } else if (DataFlavor.getTextPlainUnicodeFlavor().equals(flavor)) {
                            return content
                        }
                        throw UnsupportedFlavorException(flavor)
                    }
                })
    }
}
