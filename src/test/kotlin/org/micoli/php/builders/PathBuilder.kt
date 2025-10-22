package org.micoli.php.builders

import org.micoli.php.tasks.configuration.AbstractNode
import org.micoli.php.tasks.configuration.Path

class PathBuilder private constructor() {
    private val path: Path = Path()

    fun withLabel(label: String): PathBuilder {
        path.label = label
        return this
    }

    fun withTasks(tasks: Array<AbstractNode>): PathBuilder {
        path.tasks = tasks
        return this
    }

    fun build(): Path = path

    companion object {
        @JvmStatic fun create(): PathBuilder = PathBuilder()
    }
}
