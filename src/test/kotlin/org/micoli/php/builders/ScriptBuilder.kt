package org.micoli.php.builders

import org.micoli.php.tasks.configuration.runnableTask.Script

class ScriptBuilder private constructor() {
    private val script: Script = Script()

    fun withId(id: String): ScriptBuilder {
        script.id = id
        return this
    }

    fun withLabel(label: String): ScriptBuilder {
        script.label = label
        return this
    }

    fun withSource(source: String): ScriptBuilder {
        script.source = source
        return this
    }

    fun withExtension(extension: String): ScriptBuilder {
        script.extension = extension
        return this
    }

    fun withIcon(icon: String): ScriptBuilder {
        script.icon = icon
        return this
    }

    fun build(): Script {
        return script
    }

    companion object {
        @JvmStatic
        fun create(): ScriptBuilder {
            return ScriptBuilder()
        }
    }
}
