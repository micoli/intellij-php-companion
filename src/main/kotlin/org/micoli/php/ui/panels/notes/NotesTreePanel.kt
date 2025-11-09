package org.micoli.php.ui.panels.notes

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.ui.TreeUIHelper
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.tree.DefaultTreeModel
import org.micoli.php.configuration.models.Configuration
import org.micoli.php.events.ConfigurationEvents
import org.micoli.php.notes.NotesService
import org.micoli.php.notes.models.NoteTreeNode
import org.micoli.php.notes.models.NoteType
import org.micoli.php.ui.components.tasks.tree.notes.NotesTreeService
import org.micoli.php.ui.components.tasks.tree.notes.PathNode
import org.micoli.php.ui.components.tasks.tree.notes.TreeCellRenderer
import org.micoli.php.ui.panels.notes.actions.AddNoteAction
import org.micoli.php.ui.panels.notes.actions.AddPathAction
import org.micoli.php.ui.panels.notes.actions.DeleteItemAction
import org.micoli.php.ui.panels.notes.actions.DeselectAllAction
import org.micoli.php.ui.panels.notes.actions.ReloadTreeAction
import org.micoli.php.ui.panels.notes.actions.RenameItemAction

class NotesTreePanel(val project: Project) : JPanel(), Disposable {
    private val mainPanel: JComponent = JPanel()
    private val leftActionGroup = DefaultActionGroup()
    private val tree =
        Tree(DefaultTreeModel(PathNode(NoteTreeNode("", NoteType.PATH, "", null, null), "Notes")))
    private val notesTreeService = NotesTreeService(project, tree, this::loadNotesTree)

    init {
        this.setLayout(BorderLayout(2, 2))
        this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0))
        this.add(mainPanel, BorderLayout.CENTER)
        this.add(createToolbar(), BorderLayout.NORTH)
        mainPanel.setLayout(BorderLayout())
        tree.setCellRenderer(TreeCellRenderer())
        TreeUIHelper.getInstance().installTreeSpeedSearch(tree)

        val comp = JBScrollPane(tree)
        comp.setBorder(JBUI.Borders.empty())
        mainPanel.add(comp, BorderLayout.CENTER)
        project.messageBus
            .connect()
            .subscribe<ConfigurationEvents>(
                ConfigurationEvents.CONFIGURATION_UPDATED,
                object : ConfigurationEvents {
                    override fun configurationLoaded(loadedConfiguration: Configuration) {
                        SwingUtilities.invokeLater { refresh() }
                    }
                },
            )
    }

    fun refresh() {
        this.loadNotesTree()
        this.mainPanel.revalidate()
    }

    private fun loadNotesTree() {
        notesTreeService.configureTree(NotesService.getInstance(project).getAllNotes())
    }

    private fun createToolbar(): JComponent {
        val toolbarPanel = JPanel(BorderLayout())
        val leftToolbar =
            ActionManager.getInstance()
                .createActionToolbar("PhpCompanionNotesRightToolbar", leftActionGroup, true)
        leftToolbar.targetComponent = mainPanel

        this.leftActionGroup.add(AddNoteAction(project, notesTreeService, this::loadNotesTree))
        this.leftActionGroup.add(AddPathAction(project, notesTreeService, this::loadNotesTree))
        this.leftActionGroup.add(RenameItemAction(project, notesTreeService, this::loadNotesTree))
        this.leftActionGroup.add(DeleteItemAction(project, notesTreeService, this::loadNotesTree))
        this.leftActionGroup.add(DeselectAllAction(project, notesTreeService, tree))
        this.leftActionGroup.add(ReloadTreeAction(project, this::loadNotesTree))

        toolbarPanel.add(leftToolbar.component, BorderLayout.WEST)

        return toolbarPanel
    }

    override fun dispose() {
        notesTreeService.dispose()
    }
}
