package org.micoli.php.tasks.configuration.runnableTask

import io.swagger.v3.oas.annotations.media.Schema

class Shell : RunnableTaskConfiguration(), TaskWithIcon {
    @Schema(
        description =
            "System command to execute in shell. Can include arguments and use environment variables",
        example = "make clear-cache",
    )
    var command: String? = null

    @Schema(
        description =
            "Current working directory for command execution. If null, uses project root directory")
    var cwd: String? = null

    @Schema(
        description =
            "Path to the icon to display for this shell task. Uses standard IntelliJ Platform icons")
    override var icon: String = "debugger/threadRunning.svg"
}
