package org.micoli.php.tasks.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.v3.oas.annotations.media.Schema
import org.micoli.php.configuration.models.DisactivableConfiguration
import org.micoli.php.tasks.configuration.runnableTask.Bookmark
import org.micoli.php.tasks.configuration.runnableTask.Link
import org.micoli.php.tasks.configuration.runnableTask.RunnableTaskConfiguration

class TasksConfiguration : DisactivableConfiguration {
    override fun isDisabled(): Boolean {
        return !enabled
    }

    @Schema(description = "Enabler for panel of Task and actions", example = "true")
    var enabled: Boolean = false

    @Schema(
        description =
            ("Array of runnable task configurations available in the system. Each task must have a unique" +
                " identifier to be referenced by tree or toolbar"))
    var tasks: Array<RunnableTaskConfiguration> = arrayOf()

    @Schema(
        description =
            ("Hierarchical tree structure of tasks and folders for organization in the user interface. Can" +
                " contain Task objects (referencing tasks by ID) and Path objects (folders containing other" +
                " nodes)"))
    var tree: Array<AbstractNode> = arrayOf()

    @Schema(
        description =
            ("Array of tasks to display in the toolbar for quick access. Each element must reference an" +
                " existing task via its taskId"))
    var toolbar: Array<Task> = arrayOf()

    @Schema(
        description =
            "File watchers configuration that automatically trigger tasks when specified files are modified")
    var watchers: Array<Watcher> = arrayOf()

    @get:JsonIgnore
    val tasksMap: MutableMap<String, RunnableTaskConfiguration>
        get() =
            tasks
                .filter { it.isFullyInitialized() }
                .associateBy(
                    keySelector = { task: RunnableTaskConfiguration -> task.id },
                    valueTransform = { task: RunnableTaskConfiguration -> task },
                )
                .toMutableMap()

    @JsonIgnore
    fun assertConfigurationIsValid() {
        val tasksMap = this.tasksMap
        assertTreeNodesIsValid(tasksMap, tree)
        for (node in toolbar) {
            tasksMap[node.taskId]
        }
    }

    @JsonIgnore
    private fun assertTreeNodesIsValid(
        tasksMap: MutableMap<String, RunnableTaskConfiguration>,
        nodes: Array<AbstractNode>?,
    ) {
        if (nodes == null) {
            return
        }
        for (node in nodes) {
            when (node) {
                is Task -> tasksMap[node.taskId]
                is Path -> assertTreeNodesIsValid(tasksMap, node.tasks)
                is Link -> node.url != null && node.label != null
                is Bookmark -> node.path != null && node.label != null
                else -> throw IllegalStateException("Unexpected value: " + node.javaClass.name)
            }
        }
    }
}
