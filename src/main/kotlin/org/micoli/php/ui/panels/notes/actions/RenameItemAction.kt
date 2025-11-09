package org.micoli.php.ui.panels.notes.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import org.micoli.php.notes.NotesException
import org.micoli.php.notes.NotesService
import org.micoli.php.ui.Notification
import org.micoli.php.ui.components.tasks.tree.notes.NotesTreeService

class RenameItemAction(
    val project: Project,
    val notesTreeService: NotesTreeService,
    val refreshTree: () -> Unit
) : AnAction("Rename", "Rename selected item", AllIcons.General.Inline_edit) {
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun actionPerformed(anActionEvent: AnActionEvent) {
        val selectedNote = notesTreeService.getSelectedNote()
        val selectedPathNode = notesTreeService.getSelectedPathNode()

        when {
            selectedNote != null -> {
                val name =
                    Messages.showInputDialog(
                        project,
                        "Rename note '${selectedNote.name}'?",
                        "Rename Note",
                        Messages.getQuestionIcon(),
                        selectedNote.name,
                        null) ?: return

                if (name.isEmpty()) {
                    return
                }
                LOGGER.debug("Renaming note '${selectedNote.name}' [${selectedNote.id}]")
                try {
                    NotesService.getInstance(project)
                        .noteFilesystem
                        .renameNoteById(selectedNote.id, name)
                    refreshTree()
                } catch (e: NotesException) {
                    Notification.getInstance(project).error(e.message!!)
                }
            }
            selectedPathNode != null -> {
                val path =
                    NotesService.getInstance(project)
                        .noteFilesystem
                        .getPathByFullPath(selectedPathNode.noteTreeNode.fullPath) ?: return
                val name =
                    Messages.showInputDialog(
                        project,
                        "Rename path '${path.label}'?",
                        "Rename Path",
                        Messages.getQuestionIcon(),
                        path.label,
                        null) ?: return
                if (name.isEmpty()) {
                    return
                }
                LOGGER.debug("Renaming path '${path.label}' [${path.id}]")
                try {
                    NotesService.getInstance(project).noteFilesystem.renamePathById(path.id, name)
                    refreshTree()
                } catch (e: NotesException) {
                    Notification.getInstance(project).error(e.message!!)
                }
            }
        }
    }

    override fun update(e: AnActionEvent) {
        val hasNote = notesTreeService.getSelectedNote() != null
        val hasPath = notesTreeService.getSelectedPathNode() != null
        e.presentation.isEnabled = hasNote || hasPath
    }

    companion object {
        private val LOGGER = Logger.getInstance(RenameItemAction::class.java.getSimpleName())
    }
}
