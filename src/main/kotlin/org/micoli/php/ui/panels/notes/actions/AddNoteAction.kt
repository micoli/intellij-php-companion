package org.micoli.php.ui.panels.notes.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.ComboBoxAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.util.ui.JBInsets
import com.intellij.util.ui.accessibility.ScreenReader
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JComponent
import javax.swing.JPanel
import org.micoli.php.notes.NotesException
import org.micoli.php.notes.NotesService
import org.micoli.php.notes.models.NoteType
import org.micoli.php.ui.Notification
import org.micoli.php.ui.components.tasks.tree.notes.NotesTreeService

class AddNoteAction(
    val project: Project,
    val notesTreeService: NotesTreeService,
    val refreshTree: () -> Unit
) : ComboBoxAction() {
    init {
        myPopupTitle = "Note Types"
    }

    override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
        val button = this.createComboBoxButton(presentation)
        button.setFocusable(ScreenReader.isActive())
        button.border = null
        if (this.isNoWrapping(place)) {
            return button
        }
        val panel = JPanel(GridBagLayout())
        val constraints =
            GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, 10, 1, JBInsets.create(0, 3), 0, 0)
        panel.add(button, constraints)
        return panel
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun createPopupActionGroup(
        button: JComponent,
        dataContext: DataContext
    ): DefaultActionGroup {
        val group = DefaultActionGroup()
        NoteType.fileTypesEntries().forEach { noteType ->
            group.add(AddSpecificTypeNoteAction(noteType, project, notesTreeService, refreshTree))
        }
        return group
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        e.presentation.description = "Add a new note"
        e.presentation.icon = AllIcons.Actions.AddFile
    }

    private inner class AddSpecificTypeNoteAction(
        val noteType: NoteType,
        val project: Project,
        val notesTreeService: NotesTreeService,
        val refreshTree: () -> Unit
    ) : AnAction(noteType.name, "Add a new ${noteType.name} note", AllIcons.Actions.AddFile) {

        init {
            templatePresentation.icon = noteType.getIcon()
        }

        override fun getActionUpdateThread(): ActionUpdateThread {
            return ActionUpdateThread.BGT
        }

        override fun actionPerformed(anActionEvent: AnActionEvent) {
            val name =
                Messages.showInputDialog(
                    project,
                    "Note",
                    "Add ${noteType.name} Note",
                    Messages.getQuestionIcon(),
                ) ?: return

            if (name.isEmpty()) {
                return
            }
            LOGGER.debug("Add note '${name}' of type $noteType")
            try {
                val note =
                    NotesService.getInstance(project)
                        .noteFilesystem
                        .addNote(notesTreeService.getSelectedPath(), name, noteType, "")
                refreshTree()
                val node = notesTreeService.selectNoteInTree(note) ?: return
                NotesService.getInstance(project).openNote(note) { node.note = it }
            } catch (e: NotesException) {
                Notification.getInstance(project).error(e.message!!)
            }
        }
    }

    companion object {
        private val LOGGER = Logger.getInstance(AddNoteAction::class.java.getSimpleName())
    }
}
