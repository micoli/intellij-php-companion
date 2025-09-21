package org.micoli.php.builders

import org.micoli.php.tasks.configuration.runnableTask.Shell

class ShellBuilder private constructor() {
    private val shell: Shell = Shell()

    fun withId(id: String): ShellBuilder {
        shell.id = id
        return this
    }

    fun withLabel(label: String): ShellBuilder {
        shell.label = label
        return this
    }

    fun withCommand(command: String): ShellBuilder {
        shell.command = command
        return this
    }

    fun withCwd(cwd: String): ShellBuilder {
        shell.cwd = cwd
        return this
    }

    fun withIcon(icon: String): ShellBuilder {
        shell.icon = icon
        return this
    }

    fun build(): Shell = shell

    companion object {
        @JvmStatic fun create(): ShellBuilder = ShellBuilder()
    }
}
