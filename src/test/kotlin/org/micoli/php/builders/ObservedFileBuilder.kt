package org.micoli.php.builders

import org.micoli.php.tasks.configuration.runnableTask.ObservedFile
import org.micoli.php.tasks.configuration.runnableTask.postToggle.PostToggle

class ObservedFileBuilder() {
    private val observedFile: ObservedFile = ObservedFile(null)

    fun withId(id: String): ObservedFileBuilder {
        observedFile.id = id
        return this
    }

    fun withLabel(label: String): ObservedFileBuilder {
        observedFile.label = label
        return this
    }

    fun withCommentPrefix(commentPrefix: String): ObservedFileBuilder {
        observedFile.commentPrefix = commentPrefix
        return this
    }

    fun withFilePath(filePath: String): ObservedFileBuilder {
        observedFile.filePath = filePath
        return this
    }

    fun withVariableName(variableName: String): ObservedFileBuilder {
        observedFile.variableName = variableName
        return this
    }

    fun withActiveIcon(activeIcon: String): ObservedFileBuilder {
        observedFile.activeIcon = activeIcon
        return this
    }

    fun withInactiveIcon(inactiveIcon: String): ObservedFileBuilder {
        observedFile.inactiveIcon = inactiveIcon
        return this
    }

    fun withUnknownIcon(unknownIcon: String): ObservedFileBuilder {
        observedFile.unknownIcon = unknownIcon
        return this
    }

    fun withPostToggle(postToggle: PostToggle): ObservedFileBuilder {
        observedFile.postToggle = postToggle
        return this
    }

    fun build(): ObservedFile = observedFile

    companion object {
        fun create(): ObservedFileBuilder = ObservedFileBuilder()
    }
}
