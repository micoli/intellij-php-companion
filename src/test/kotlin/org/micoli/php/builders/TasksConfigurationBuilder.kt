package org.micoli.php.builders

import org.micoli.php.tasks.configuration.AbstractNode
import org.micoli.php.tasks.configuration.Task
import org.micoli.php.tasks.configuration.TasksConfiguration
import org.micoli.php.tasks.configuration.Watcher
import org.micoli.php.tasks.configuration.runnableTask.RunnableTaskConfiguration

class TasksConfigurationBuilder private constructor() {
    private val taskConfiguration: TasksConfiguration = TasksConfiguration()

    fun withRunnableTaskConfigurations(
        tasks: Array<RunnableTaskConfiguration>
    ): TasksConfigurationBuilder {
        taskConfiguration.tasks = tasks
        return this
    }

    fun withAddedRunnableTaskConfiguration(
        task: RunnableTaskConfiguration
    ): TasksConfigurationBuilder {
        taskConfiguration.tasks =
            appendToArray(taskConfiguration.tasks, task, RunnableTaskConfiguration::class.java)
        return this
    }

    fun withAbstractNodes(nodes: Array<AbstractNode>): TasksConfigurationBuilder {
        taskConfiguration.tree = nodes
        return this
    }

    fun withAddedAbstractNode(node: AbstractNode): TasksConfigurationBuilder {
        taskConfiguration.tree =
            appendToArray(taskConfiguration.tree, node, AbstractNode::class.java)
        return this
    }

    fun withWatchers(watchers: Array<Watcher>): TasksConfigurationBuilder {
        taskConfiguration.watchers = watchers
        return this
    }

    fun withAddedWatcher(watcher: Watcher): TasksConfigurationBuilder {
        taskConfiguration.watchers =
            appendToArray(taskConfiguration.watchers, watcher, Watcher::class.java)
        return this
    }

    fun withToolbarTasks(tasks: Array<Task>): TasksConfigurationBuilder {
        taskConfiguration.toolbar = tasks
        return this
    }

    fun withAddedTaskInToolbar(task: Task): TasksConfigurationBuilder {
        taskConfiguration.toolbar = appendToArray(taskConfiguration.toolbar, task, Task::class.java)
        return this
    }

    fun withEnabled(enabled: Boolean): TasksConfigurationBuilder {
        taskConfiguration.enabled = enabled
        return this
    }

    fun build(): TasksConfiguration = taskConfiguration

    companion object {
        fun create(): TasksConfigurationBuilder = TasksConfigurationBuilder()
    }
}
