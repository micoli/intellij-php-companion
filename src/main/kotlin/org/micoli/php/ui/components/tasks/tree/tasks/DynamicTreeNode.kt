package org.micoli.php.ui.components.tasks.tree.tasks

import com.intellij.openapi.project.Project
import com.intellij.ui.treeStructure.Tree
import javax.swing.Icon
import javax.swing.tree.DefaultTreeModel
import org.micoli.php.tasks.TasksService
import org.micoli.php.tasks.configuration.runnableTask.RunnableTaskConfiguration

open class DynamicTreeNode(
    private val project: Project,
    private val tree: Tree,
    protected val taskId: String,
    var icon: Icon?,
    label: String,
    configurationNode: RunnableTaskConfiguration,
) : LabeledTreeNode(configurationNode, label) {
    protected fun setIconAndLabel(icon: Icon?, label: String) {
        this.icon = icon
        setLabel(label)
        (tree.model as DefaultTreeModel).reload(this)
    }

    fun run() {
        TasksService.getInstance(project).runTask(taskId)
    }
}
