package org.micoli.php.ui.components.tasks.tree.notes.dragAndDrop

import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException

class PathTransferable(private val pathId: String) : Transferable {
    override fun getTransferDataFlavors(): Array<DataFlavor> = arrayOf(PATH_FLAVOR)

    override fun isDataFlavorSupported(flavor: DataFlavor): Boolean = flavor == PATH_FLAVOR

    override fun getTransferData(flavor: DataFlavor): Any {
        if (!isDataFlavorSupported(flavor)) {
            throw UnsupportedFlavorException(flavor)
        }
        return pathId
    }

    companion object {
        val PATH_FLAVOR =
            DataFlavor(
                "application/x-java-jvm-local-objectref-path; class=java.lang.String", "PathId")
    }
}
