package org.micoli.php.ui.panels.notes.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.micoli.php.notes.NotesException
import org.micoli.php.notes.NotesService
import org.micoli.php.ui.Notification
import org.micoli.php.ui.components.tasks.InputDialogWithCheckbox
import org.micoli.php.ui.components.tasks.tree.notes.NotesTreeService

class AddPathAction(
    val project: Project,
    val notesTreeService: NotesTreeService,
    val refreshTree: () -> Unit
) : AnAction("Add Path", "Add a new folder path", AllIcons.Actions.NewFolder) {
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun actionPerformed(anActionEvent: AnActionEvent) {
        val selectedPath = notesTreeService.getSelectedPath()
        val dialog =
            InputDialogWithCheckbox(
                project,
                "Enter folder name:",
                "Add Path",
                "Is Root based",
                checkboxInitialValue = selectedPath == null,
                selectedPath == null)

        if (!dialog.showAndGet()) {
            return
        }
        val pathName = dialog.getInputText()
        val isAtRoot = dialog.isCheckboxSelected()

        if (pathName.isNotEmpty()) {
            LOGGER.debug("Add path '${pathName}' [isAtRoot: ${isAtRoot}]")
            try {
                NotesService.getInstance(project)
                    .noteFilesystem
                    .addPath(if (isAtRoot) null else selectedPath, pathName)
                refreshTree()
            } catch (e: NotesException) {
                Notification.getInstance(project).error(e.message!!)
            }
        }
    }

    companion object {
        private val LOGGER = Logger.getInstance(AddPathAction::class.java.getSimpleName())
    }
}
