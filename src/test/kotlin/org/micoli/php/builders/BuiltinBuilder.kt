package org.micoli.php.builders

import org.micoli.php.tasks.configuration.runnableTask.Builtin

class BuiltinBuilder private constructor() {
    private val shell: Builtin = Builtin()

    fun withId(id: String): BuiltinBuilder {
        shell.id = id
        return this
    }

    fun withLabel(label: String): BuiltinBuilder {
        shell.label = label
        return this
    }

    fun withActionId(actionId: String): BuiltinBuilder {
        shell.actionId = actionId
        return this
    }

    fun withIcon(icon: String): BuiltinBuilder {
        shell.icon = icon
        return this
    }

    fun build(): Builtin = shell

    companion object {
        fun create(): BuiltinBuilder = BuiltinBuilder()
    }
}
