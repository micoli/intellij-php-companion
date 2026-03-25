package org.micoli.php.ui.components.tasks.tree.tasks

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonShortcuts
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader.getIcon
import com.intellij.ui.treeStructure.Tree
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath
import kotlin.collections.get
import org.micoli.php.tasks.configuration.AbstractNode
import org.micoli.php.tasks.configuration.Path
import org.micoli.php.tasks.configuration.Task
import org.micoli.php.tasks.configuration.runnableTask.Bookmark
import org.micoli.php.tasks.configuration.runnableTask.Link
import org.micoli.php.tasks.configuration.runnableTask.ObservedFile
import org.micoli.php.tasks.configuration.runnableTask.RunnableTaskConfiguration
import org.micoli.php.tasks.configuration.runnableTask.Script
import org.micoli.php.tasks.configuration.runnableTask.Shell
import org.micoli.php.ui.PhpCompanionIcon
import org.micoli.php.ui.components.tasks.tree.TreeIterator

class ActionTreeNodeConfigurator(private val project: Project, private val tree: Tree) {
    private val treeModel: DefaultTreeModel = tree.model as DefaultTreeModel
    private val root: DefaultMutableTreeNode = treeModel.getRoot() as DefaultMutableTreeNode

    init {
        this.registerDoubleClickAction(tree)
        this.registerEnterKeyAction(tree)
    }

    fun configureTree(
        runnables: Map<String, RunnableTaskConfiguration>,
        nodes: Array<AbstractNode>?
    ) {
        cleanup()
        treeModel.reload()

        addSubNodes(tree, root, runnables, nodes)
        treeModel.reload()
        tree.setRootVisible(false)
        tree.setShowsRootHandles(true)
        tree.repaint()

        TreeIterator.forEach(tree) {
            _: DefaultMutableTreeNode?,
            isLeaf: Boolean,
            _: TreePath?,
            level: Int,
            index: Int ->
            if (!isLeaf && level <= 1) {
                tree.expandRow(index)
            }
        }
    }

    private fun addSubNodes(
        tree: Tree,
        parent: DefaultMutableTreeNode,
        runnables: Map<String, RunnableTaskConfiguration>,
        nodes: Array<AbstractNode>?,
    ) {
        if (nodes == null) {
            return
        }
        for (node in nodes) {
            when (node) {
                is Task -> {
                    val runnable = runnables[node.taskId]
                    if (runnable == null) {
                        LOGGER.warn("Runnable not found for task ID: " + node.taskId)
                        continue
                    }

                    parent.add(createTaskNode(tree, node, runnable))
                    treeModel.nodesWereInserted(parent, intArrayOf(parent.childCount - 1))
                }

                is Path -> {
                    val treeNode = PathNode(node, node.label!!)
                    parent.add(treeNode)
                    treeModel.nodesWereInserted(parent, intArrayOf(parent.childCount - 1))
                    addSubNodes(tree, treeNode, runnables, node.tasks)
                }

                else -> throw IllegalStateException("Unexpected value: $node")
            }
        }
    }

    private fun createTaskNode(
        tree: Tree,
        task: Task,
        runnable: RunnableTaskConfiguration
    ): DefaultMutableTreeNode {
        val label = task.label ?: runnable.label ?: runnable.id

        return when (runnable) {
            is Shell ->
                DynamicTreeNode(
                    project,
                    tree,
                    runnable.id,
                    getIcon(runnable.icon, PhpCompanionIcon::class.java),
                    label,
                    runnable,
                )
            is Link ->
                DynamicTreeNode(
                    project,
                    tree,
                    runnable.id,
                    getIcon(runnable.icon, PhpCompanionIcon::class.java),
                    label,
                    runnable,
                )
            is Bookmark ->
                DynamicTreeNode(
                    project,
                    tree,
                    runnable.id,
                    getIcon(runnable.icon, PhpCompanionIcon::class.java),
                    label,
                    runnable,
                )
            is Script ->
                DynamicTreeNode(
                    project,
                    tree,
                    runnable.id,
                    getIcon(runnable.icon, PhpCompanionIcon::class.java),
                    label,
                    runnable,
                )

            is ObservedFile -> FileObserverNode(project, tree, label, runnable)

            else -> throw IllegalStateException("Unexpected value: $runnable")
        }
    }

    private fun registerDoubleClickAction(tree: Tree) {
        tree.addMouseListener(
            object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (e.getClickCount() != 2) {
                        return
                    }
                    val path =
                        tree.getPathForRow(tree.getClosestRowForLocation(e.getX(), e.getY()))
                            ?: return
                    val node = path.lastPathComponent as DefaultMutableTreeNode?
                    handleLeafAction(node, tree)
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

    private fun handleLeafAction(node: DefaultMutableTreeNode?, tree: Tree) {
        if (node == null) return
        if (node is DynamicTreeNode) {
            node.run()
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
        TreeIterator.forEach(tree) {
            node: DefaultMutableTreeNode?,
            _: Boolean,
            _: TreePath?,
            _: Int,
            _: Int ->
            if (node is Disposable) {
                node.dispose()
            }
        }
        root.removeAllChildren()
    }

    companion object {
        private val LOGGER: Logger =
            Logger.getInstance(ActionTreeNodeConfigurator::class.java.getSimpleName())
    }
}
