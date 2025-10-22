package org.micoli.php.builders

import org.micoli.php.tasks.configuration.Task

class TaskBuilder private constructor() {
    private val task: Task = Task()

    fun withTaskId(taskId: String): TaskBuilder {
        task.taskId = taskId
        return this
    }

    fun withLabel(label: String): TaskBuilder {
        task.label = label
        return this
    }

    fun build(): Task = task

    companion object {
        @JvmStatic fun create(): TaskBuilder = TaskBuilder()
    }
}
