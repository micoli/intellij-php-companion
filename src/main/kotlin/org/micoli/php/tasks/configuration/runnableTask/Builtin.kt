package org.micoli.php.tasks.configuration.runnableTask

import io.swagger.v3.oas.annotations.media.Schema

class Builtin : RunnableTaskConfiguration(), TaskWithIcon {
    @Schema(description = "Builtin actionId to execute", example = "\$Copy")
    var actionId: String? = null

    @Schema(
        description =
            "Path to the icon to display for this builtin task. Uses standard IntelliJ Platform icons")
    override var icon: String = "debugger/threadRunning.svg"
}
