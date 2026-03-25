package org.micoli.php.ui.panels.notes.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import org.micoli.php.notes.NotesService
import org.micoli.php.ui.components.tasks.tree.notes.NotesTreeService

class DeleteItemAction(
    val project: Project,
    val notesTreeService: NotesTreeService,
    val refreshTree: () -> Unit
) : AnAction("Delete", "Delete selected item", AllIcons.General.Delete) {
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun actionPerformed(anActionEvent: AnActionEvent) {
        val selectedNote = notesTreeService.getSelectedNote()
        val selectedPathNode = notesTreeService.getSelectedPathNode()

        when {
            selectedNote != null -> {
                if (Messages.YES !=
                    Messages.showYesNoDialog(
                        project,
                        "Delete note '${selectedNote.name}'?",
                        "Delete Note",
                        Messages.getQuestionIcon())) {
                    return
                }
                LOGGER.debug("Deleting note '${selectedNote.name}' [${selectedNote.id}]")
                NotesService.getInstance(project).noteFilesystem.deleteNoteById(selectedNote.id)
                refreshTree()
            }
            selectedPathNode != null -> {
                val path =
                    NotesService.getInstance(project)
                        .noteFilesystem
                        .getPathByFullPath(selectedPathNode.noteTreeNode.fullPath) ?: return
                if (Messages.showYesNoDialog(
                    project,
                    "Delete folder '${selectedPathNode.getLabel()}' and all its contents?",
                    "Delete Folder",
                    Messages.getQuestionIcon()) != Messages.YES) {
                    return
                }
                LOGGER.debug("Deleting path '${selectedPathNode.getLabel()}' [${path.id}]")
                NotesService.getInstance(project).noteFilesystem.deletePathById(path.id)
                refreshTree()
            }
        }
    }

    override fun update(e: AnActionEvent) {
        val hasNote = notesTreeService.getSelectedNote() != null
        val hasPath = notesTreeService.getSelectedPathNode() != null
        e.presentation.isEnabled = hasNote || hasPath
    }

    companion object {
        private val LOGGER = Logger.getInstance(DeleteItemAction::class.java.getSimpleName())
    }
}
