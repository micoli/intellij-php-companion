package org.micoli.php.ui.panels

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
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
import org.micoli.php.service.TaskScheduler
import org.micoli.php.tasks.TasksService
import org.micoli.php.tasks.configuration.TasksConfiguration
import org.micoli.php.tasks.configuration.runnableTask.Builtin
import org.micoli.php.tasks.configuration.runnableTask.ObservedFile
import org.micoli.php.tasks.configuration.runnableTask.Script
import org.micoli.php.tasks.configuration.runnableTask.Shell
import org.micoli.php.ui.components.tasks.toolbar.FileObserverToolbarButton
import org.micoli.php.ui.components.tasks.toolbar.TaskToolbarButton
import org.micoli.php.ui.components.tasks.tree.ActionTreeNodeConfigurator
import org.micoli.php.ui.components.tasks.tree.PathNode
import org.micoli.php.ui.components.tasks.tree.TreeCellRenderer

class ActionTreePanel(project: Project) : JPanel(), Disposable {
    private val mainPanel: JComponent = JPanel()
    private val project: Project
    private val actionTreeNodeConfigurator: ActionTreeNodeConfigurator
    private val leftActionGroup = DefaultActionGroup()

    init {
        this.setLayout(BorderLayout(2, 2))
        this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0))
        this.add(mainPanel, BorderLayout.CENTER)
        this.add(createToolbar(), BorderLayout.NORTH)
        this.project = project
        mainPanel.setLayout(BorderLayout())
        val tree = Tree(DefaultTreeModel(PathNode(Any(), "Actions")))
        tree.setCellRenderer(TreeCellRenderer())
        val comp = JBScrollPane(tree)
        comp.setBorder(JBUI.Borders.empty())
        mainPanel.add(comp, BorderLayout.CENTER)
        actionTreeNodeConfigurator = ActionTreeNodeConfigurator(project, tree)
        project.messageBus
            .connect()
            .subscribe<ConfigurationEvents>(
                ConfigurationEvents.CONFIGURATION_UPDATED,
                ConfigurationEvents { configuration: Configuration? ->
                    SwingUtilities.invokeLater {
                        this.loadButtonBar(project, configuration!!.tasksConfiguration)
                        this.loadActionTree(configuration.tasksConfiguration)
                        refresh()
                    }
                },
            )
    }

    fun refresh() {
        this.mainPanel.revalidate()
        TaskScheduler.scheduleLater(
            {
                LOGGER.warn("Refreshing all file observers")
                TasksService.getInstance(project).refreshObservedFiles(true)
            },
            1000,
        )
    }

    private fun loadActionTree(configuration: TasksConfiguration?) {
        if (configuration == null) {
            return
        }
        actionTreeNodeConfigurator.configureTree(configuration.tasksMap, configuration.tree)
    }

    private fun loadButtonBar(project: Project, configuration: TasksConfiguration?) {
        val actionManager = ActionManager.getInstance()
        cleanupActionGroup(actionManager)

        if (configuration == null) {
            return
        }

        for (toolbarTask in configuration.toolbar) {
            val task = configuration.tasksMap[toolbarTask.taskId] ?: continue
            val action =
                when (task) {
                    is Builtin -> TaskToolbarButton(project, task)
                    is Shell -> TaskToolbarButton(project, task)
                    is Script -> TaskToolbarButton(project, task)
                    is ObservedFile -> FileObserverToolbarButton(project, task)
                    else -> throw IllegalStateException("Unexpected value: $task")
                }

            actionManager.registerAction(
                "PhpCompanion." + task.javaClass.getSimpleName() + "." + task.id, action)
            this.leftActionGroup.add(action)
        }
    }

    private fun createToolbar(): JComponent {
        val toolbarPanel = JPanel(BorderLayout())
        val leftToolbar =
            ActionManager.getInstance()
                .createActionToolbar("PhpCompanionRightToolbar", leftActionGroup, true)
        leftToolbar.targetComponent = mainPanel
        toolbarPanel.add(leftToolbar.component, BorderLayout.WEST)

        return toolbarPanel
    }

    override fun dispose() {
        cleanupActionGroup(ActionManager.getInstance())
    }

    private fun cleanupActionGroup(actionManager: ActionManager) {
        for (action in this.leftActionGroup.getChildren(actionManager)) {
            val actionId = actionManager.getId(action)
            if (actionId != null) {
                actionManager.unregisterAction(actionId)
            }

            if (action is Disposable) {
                action.dispose()
            }
        }

        this.leftActionGroup.removeAll()
    }

    companion object {
        private val LOGGER = Logger.getInstance(ActionTreePanel::class.java.getSimpleName())
    }
}
