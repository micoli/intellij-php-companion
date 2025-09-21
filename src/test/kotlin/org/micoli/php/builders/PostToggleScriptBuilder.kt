package org.micoli.php.builders

import org.micoli.php.tasks.configuration.runnableTask.postToggle.PostToggleScript

class PostToggleScriptBuilder private constructor() {
    private val script: PostToggleScript = PostToggleScript()

    fun withId(id: String): PostToggleScriptBuilder {
        script.id = id
        return this
    }

    fun withLabel(label: String): PostToggleScriptBuilder {
        script.label = label
        return this
    }

    fun withSource(source: String): PostToggleScriptBuilder {
        script.source = source
        return this
    }

    fun withExtension(extension: String): PostToggleScriptBuilder {
        script.extension = extension
        return this
    }

    fun build(): PostToggleScript = script

    companion object {
        fun create(): PostToggleScriptBuilder = PostToggleScriptBuilder()
    }
}
