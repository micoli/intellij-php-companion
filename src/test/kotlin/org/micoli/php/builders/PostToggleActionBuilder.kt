package org.micoli.php.builders

import org.micoli.php.tasks.configuration.runnableTask.postToggle.PostToggleShell

class PostToggleActionBuilder private constructor() {
    private val action: PostToggleShell = PostToggleShell()

    fun withId(id: String): PostToggleActionBuilder {
        action.id = id
        return this
    }

    fun withLabel(label: String): PostToggleActionBuilder {
        action.label = label
        return this
    }

    fun withCommand(command: String): PostToggleActionBuilder {
        action.command = command
        return this
    }

    fun withCwd(cwd: String): PostToggleActionBuilder {
        action.cwd = cwd
        return this
    }

    fun build(): PostToggleShell = action

    companion object {
        fun create(): PostToggleActionBuilder = PostToggleActionBuilder()
    }
}
