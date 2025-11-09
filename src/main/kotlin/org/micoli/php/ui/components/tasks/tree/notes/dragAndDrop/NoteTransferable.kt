package org.micoli.php.ui.components.tasks.tree.notes.dragAndDrop

import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException

class NoteTransferable(private val noteId: String) : Transferable {
    override fun getTransferDataFlavors(): Array<DataFlavor> = arrayOf(NOTE_FLAVOR)

    override fun isDataFlavorSupported(flavor: DataFlavor): Boolean = flavor == NOTE_FLAVOR

    override fun getTransferData(flavor: DataFlavor): Any {
        if (!isDataFlavorSupported(flavor)) {
            throw UnsupportedFlavorException(flavor)
        }
        return noteId
    }

    companion object {
        val NOTE_FLAVOR =
            DataFlavor("application/x-java-jvm-local-objectref; class=java.lang.String", "NoteId")
    }
}
