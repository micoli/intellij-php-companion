package org.micoli.php.tasks.configuration.runnableTask

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.swagger.v3.oas.annotations.media.Schema

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = Builtin::class)
@JsonSubTypes(
    JsonSubTypes.Type(name = "builtin", value = Builtin::class),
    JsonSubTypes.Type(name = "shell", value = Shell::class),
    JsonSubTypes.Type(name = "script", value = Script::class),
    JsonSubTypes.Type(name = "observedFile", value = ObservedFile::class),
)
abstract class RunnableTaskConfiguration {
    @Schema(
        description =
            "Unique task identifier used for references in tree and toolbar. Must be unique among all tasks in the configuration",
        example = "aTaskId",
    )
    var id: String? = null

    @Schema(
        description =
            "Label displayed to user in the interface. User-friendly name describing the task function",
        example = "First task",
    )
    var label: String? = null

    abstract val icon: String?
}
