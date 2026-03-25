package org.micoli.php.ui.panels.notes.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.ui.treeStructure.Tree
import org.micoli.php.ui.components.tasks.tree.notes.NotesTreeService

class DeselectAllAction(
    val project: Project,
    val notesTreeService: NotesTreeService,
    val tree: Tree,
) : AnAction("Clear Selection", "Clear selected items", AllIcons.Actions.Unselectall) {
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun actionPerformed(anActionEvent: AnActionEvent) {
        tree.clearSelection()
    }

    override fun update(e: AnActionEvent) {
        val hasNote = notesTreeService.getSelectedNote() != null
        val hasPath = notesTreeService.getSelectedPathNode() != null
        e.presentation.isEnabled = hasNote || hasPath
    }
}
