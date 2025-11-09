package org.micoli.php.ui.components.tasks.tree.notes.dragAndDrop

import com.intellij.openapi.project.Project
import java.awt.datatransfer.Transferable
import javax.swing.JComponent
import javax.swing.JTree
import javax.swing.TransferHandler
import javax.swing.tree.DefaultMutableTreeNode
import org.micoli.php.notes.NotesException
import org.micoli.php.notes.NotesService
import org.micoli.php.ui.Notification
import org.micoli.php.ui.components.tasks.tree.notes.NoteNode
import org.micoli.php.ui.components.tasks.tree.notes.NotesTreeService
import org.micoli.php.ui.components.tasks.tree.notes.PathNode

class NotesTreeTransferHandler(
    private val project: Project,
    private val notesTreeService: NotesTreeService
) : TransferHandler() {

    override fun getSourceActions(c: JComponent): Int {
        return MOVE
    }

    override fun createTransferable(c: JComponent): Transferable? {
        if (c !is JTree) return null

        val selectionPath = c.selectionPath ?: return null
        val node = selectionPath.lastPathComponent as? DefaultMutableTreeNode ?: return null

        return when (node) {
            is NoteNode -> NoteTransferable(node.note.id)
            is PathNode -> PathTransferable(node.noteTreeNode.pathId ?: return null)
            else -> null
        }
    }

    override fun canImport(support: TransferSupport): Boolean {
        if (!support.isDrop) return false

        val dropLocation = support.dropLocation as? JTree.DropLocation ?: return false
        val targetPath = dropLocation.path ?: return false
        val targetNode = targetPath.lastPathComponent as? DefaultMutableTreeNode ?: return false

        val isRootNode = targetNode.parent == null

        if (!isRootNode && targetNode !is PathNode) {
            return false
        }

        if (support.isDataFlavorSupported(NoteTransferable.NOTE_FLAVOR)) {
            return true
        }

        if (support.isDataFlavorSupported(PathTransferable.PATH_FLAVOR)) {
            try {
                val sourcePathId =
                    support.transferable.getTransferData(PathTransferable.PATH_FLAVOR) as String

                if (isRootNode) {
                    return true
                }

                val targetPathId = (targetNode as PathNode).noteTreeNode.pathId

                if (sourcePathId == targetPathId) {
                    return false
                }

                val notesService = NotesService.getInstance(project)
                val targetFullPath = notesService.noteFilesystem.getFullPath(targetPathId)
                val sourceFullPath = notesService.noteFilesystem.getFullPath(sourcePathId)

                return !targetFullPath.startsWith("$sourceFullPath/")
            } catch (_: Exception) {
                return false
            }
        }

        return false
    }

    override fun importData(support: TransferSupport): Boolean {
        if (!canImport(support)) return false

        val dropLocation = support.dropLocation as? JTree.DropLocation ?: return false
        val targetPath = dropLocation.path ?: return false
        val targetNode = targetPath.lastPathComponent as? DefaultMutableTreeNode ?: return false

        val targetPathId =
            when {
                targetNode is PathNode -> targetNode.noteTreeNode.pathId
                targetNode.parent == null -> null
                else -> return false
            }

        val notesService = NotesService.getInstance(project)

        try {
            when {
                support.isDataFlavorSupported(NoteTransferable.NOTE_FLAVOR) -> {
                    val noteId =
                        support.transferable.getTransferData(NoteTransferable.NOTE_FLAVOR) as String
                    notesService.noteFilesystem.moveNote(noteId, targetPathId)
                    notesTreeService.refreshTreeCallback()
                    return true
                }
                support.isDataFlavorSupported(PathTransferable.PATH_FLAVOR) -> {
                    val pathId =
                        support.transferable.getTransferData(PathTransferable.PATH_FLAVOR) as String
                    notesService.noteFilesystem.movePath(pathId, targetPathId)
                    notesTreeService.refreshTreeCallback()
                    return true
                }
            }
        } catch (e: NotesException) {
            Notification.getInstance(project).error(e.message ?: "Error moving item")
            return false
        } catch (e: Exception) {
            Notification.getInstance(project).error("Unexpected error: ${e.message}")
            return false
        }
        return false
    }
}
