package org.micoli.php.tasks.configuration

import io.swagger.v3.oas.annotations.media.Schema

class Task : AbstractNode() {
    @Schema(
        description =
            ("Identifier of the referenced task. Must match the ID of an existing task in the" +
                " TasksConfiguration tasks array"),
        example = "aTaskId",
    )
    var taskId: String? = null

    @Schema(description = "If set, it will overide task label in tree", example = "aLabel")
    var label: String? = null
}
