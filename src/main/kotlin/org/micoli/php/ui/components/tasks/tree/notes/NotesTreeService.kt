package org.micoli.php.ui.components.tasks.tree.notes

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.CommonShortcuts
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.treeStructure.Tree
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.event.TreeExpansionEvent
import javax.swing.event.TreeExpansionListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath
import org.micoli.php.notes.NotesException
import org.micoli.php.notes.NotesService
import org.micoli.php.notes.models.Note
import org.micoli.php.notes.models.NoteTreeNode
import org.micoli.php.notes.models.NotesTree
import org.micoli.php.ui.Notification
import org.micoli.php.ui.components.tasks.tree.notes.dragAndDrop.NotesTreeTransferHandler

class NotesTreeService(
    private val project: Project,
    private val tree: Tree,
    val refreshTreeCallback: (() -> Unit)
) : Disposable {
    private val treeModel: DefaultTreeModel = tree.model as DefaultTreeModel
    private val root: DefaultMutableTreeNode = treeModel.getRoot() as DefaultMutableTreeNode
    private var isLoadingTree = false

    init {
        this.registerClickAction(tree)
        this.registerExpansionListener(tree)
        this.registerPopupMenu(tree)
        this.registerEnterKeyAction(tree)
        this.registerDragAndDrop(tree)
    }

    fun configureTree(noteTree: NotesTree) {
        isLoadingTree = true
        cleanup()
        treeModel.reload()

        addNoteTreeNode(root, noteTree.root)

        treeModel.reload()
        tree.setRootVisible(false)
        tree.setShowsRootHandles(true)
        tree.repaint()

        restoreExpansionState()
        isLoadingTree = false
    }

    fun getSelectedPath(): String? {
        val selectedPath = tree.selectionPath ?: return null

        for (i in selectedPath.pathCount - 1 downTo 1) {
            val node = selectedPath.getPathComponent(i) as DefaultMutableTreeNode
            if (node is PathNode) {
                return node.noteTreeNode.pathId
            }
        }

        return null
    }

    fun getSelectedPathNode(): PathNode? {
        val selectedPath = tree.selectionPath ?: return null
        val node = selectedPath.lastPathComponent as? DefaultMutableTreeNode
        return node as? PathNode
    }

    fun getSelectedNote(): Note? {
        val selectedPath = tree.selectionPath ?: return null
        val node = selectedPath.lastPathComponent as? DefaultMutableTreeNode
        return if (node is NoteNode) node.note else null
    }

    private fun registerClickAction(tree: Tree) {
        tree.addMouseListener(
            object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    when (e.clickCount) {
                        2 -> {
                            val path =
                                tree.getPathForRow(tree.getClosestRowForLocation(e.x, e.y))
                                    ?: return
                            val node = path.lastPathComponent as DefaultMutableTreeNode?
                            handleLeafAction(node, tree)
                        }
                    }
                }
            })
    }

    private fun registerExpansionListener(tree: Tree) {
        val notesService = NotesService.getInstance(project)
        tree.addTreeExpansionListener(
            object : TreeExpansionListener {
                override fun treeExpanded(event: TreeExpansionEvent) {
                    if (isLoadingTree) return

                    val node = event.path.lastPathComponent
                    if (node is PathNode) {
                        val pathId = node.noteTreeNode.pathId
                        if (pathId != null) {
                            notesService.noteFilesystem.updatePathExpansion(pathId, true)
                        }
                    }
                }

                override fun treeCollapsed(event: TreeExpansionEvent) {
                    if (isLoadingTree) return

                    val node = event.path.lastPathComponent
                    if (node is PathNode) {
                        val pathId = node.noteTreeNode.pathId
                        if (pathId != null) {
                            notesService.noteFilesystem.updatePathExpansion(pathId, false)
                        }
                    }
                }
            })
    }

    private fun registerPopupMenu(tree: Tree) {
        tree.addMouseListener(
            object : MouseAdapter() {
                override fun mousePressed(e: MouseEvent) {
                    if (e.isPopupTrigger) {
                        showPopupMenu(e)
                    }
                }

                override fun mouseReleased(e: MouseEvent) {
                    if (e.isPopupTrigger) {
                        showPopupMenu(e)
                    }
                }

                private fun showPopupMenu(e: MouseEvent) {
                    val path = tree.getPathForLocation(e.x, e.y) ?: return
                    tree.selectionPath = path

                    val node = path.lastPathComponent as? DefaultMutableTreeNode ?: return
                    if (node !is NoteNode && node !is PathNode) return

                    JBPopupFactory.getInstance()
                        .createActionGroupPopup(
                            null,
                            createPopupActionGroup(),
                            SimpleDataContext.builder()
                                .add(CommonDataKeys.PROJECT, project)
                                .build(),
                            JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                            true)
                        .show(com.intellij.ui.awt.RelativePoint(e))
                }

                private fun createPopupActionGroup(): ActionGroup {
                    val group = com.intellij.openapi.actionSystem.DefaultActionGroup()
                    group.add(createRenameAction())
                    return group
                }

                private fun createRenameAction(): AnAction {
                    return object :
                        AnAction(
                            "Rename",
                            "Rename the selected item",
                            com.intellij.icons.AllIcons.General.Inline_edit) {
                        override fun actionPerformed(e: AnActionEvent) {
                            performRename()
                        }

                        override fun getActionUpdateThread(): ActionUpdateThread {
                            return ActionUpdateThread.BGT
                        }
                    }
                }

                private fun performRename() {
                    val selectedNote = getSelectedNote()
                    val selectedPathNode = getSelectedPathNode()

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
                            try {
                                NotesService.getInstance(project)
                                    .noteFilesystem
                                    .renameNoteById(selectedNote.id, name)
                                refreshTreeCallback()
                            } catch (e: NotesException) {
                                Notification.getInstance(project).error(e.message!!)
                            }
                        }
                        selectedPathNode != null -> {
                            val path =
                                NotesService.getInstance(project)
                                    .noteFilesystem
                                    .getPathByFullPath(selectedPathNode.noteTreeNode.fullPath)
                                    ?: return
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
                            try {
                                NotesService.getInstance(project)
                                    .noteFilesystem
                                    .renamePathById(path.id, name)
                                refreshTreeCallback()
                            } catch (e: NotesException) {
                                Notification.getInstance(project).error(e.message!!)
                            }
                        }
                    }
                }
            })
    }

    private fun registerEnterKeyAction(tree: Tree) {
        val enterAction: AnAction =
            object : AnAction() {
                override fun actionPerformed(e: AnActionEvent) {
                    val selectedPath = tree.selectionPath
                    if (selectedPath != null) {
                        handleLeafAction(
                            selectedPath.lastPathComponent as DefaultMutableTreeNode?, tree)
                    }
                }
            }

        enterAction.registerCustomShortcutSet(CommonShortcuts.ENTER, tree)
    }

    private fun registerDragAndDrop(tree: Tree) {
        tree.dragEnabled = true
        tree.dropMode = javax.swing.DropMode.ON
        tree.transferHandler = NotesTreeTransferHandler(project, this)
    }

    private fun addNoteTreeNode(parent: DefaultMutableTreeNode, noteTreeNode: NoteTreeNode) {
        noteTreeNode.children.values
            .sortedBy { it.name }
            .forEach { childNode ->
                if (childNode.isLeaf()) {
                    val noteNode = NoteNode(project, tree, childNode.note!!)
                    parent.add(noteNode)
                    treeModel.nodesWereInserted(parent, intArrayOf(parent.childCount - 1))
                } else {
                    val pathNode = PathNode(childNode, childNode.name)
                    parent.add(pathNode)
                    treeModel.nodesWereInserted(parent, intArrayOf(parent.childCount - 1))
                    addNoteTreeNode(pathNode, childNode)
                }
            }
    }

    private fun handleLeafAction(node: DefaultMutableTreeNode?, tree: Tree) {
        if (node == null) return

        if (node is NoteNode) {
            node.openNote()
            return
        }

        val path = TreePath(node.path)
        if (tree.isExpanded(path)) {
            tree.collapsePath(path)
            return
        }
        tree.expandPath(path)
    }

    private fun cleanup() {
        root.removeAllChildren()
    }

    override fun dispose() {
        cleanup()
    }

    private fun restoreExpansionState() {
        val rootNode = tree.model.root as DefaultMutableTreeNode
        restoreExpansionStateRecursive(rootNode, TreePath(rootNode))
    }

    private fun restoreExpansionStateRecursive(node: DefaultMutableTreeNode, path: TreePath) {
        if (node is PathNode) {
            val pathId = node.noteTreeNode.pathId
            if (pathId != null) {
                if (NotesService.getInstance(project)
                    .noteFilesystem
                    .getPathExpansionState(pathId)) {
                    tree.expandPath(path)
                } else {
                    tree.collapsePath(path)
                }
            }
        }

        for (i in 0 until node.childCount) {
            val child = node.getChildAt(i) as DefaultMutableTreeNode
            restoreExpansionStateRecursive(child, path.pathByAddingChild(child))
        }
    }

    fun selectNoteInTree(note: Note): NoteNode? {
        val rootNode = tree.model.root as DefaultMutableTreeNode
        val noteNode = findNoteNode(rootNode, note.id)

        if (noteNode != null) {
            val path = TreePath(noteNode.path)
            tree.selectionPath = path
            tree.scrollPathToVisible(path)
        }
        return noteNode
    }

    private fun findNoteNode(node: DefaultMutableTreeNode, noteId: String): NoteNode? {
        if (node is NoteNode && node.note.id == noteId) {
            return node
        }

        for (i in 0 until node.childCount) {
            val child = node.getChildAt(i) as DefaultMutableTreeNode
            val found = findNoteNode(child, noteId)
            if (found != null) {
                return found
            }
        }

        return null
    }
}
