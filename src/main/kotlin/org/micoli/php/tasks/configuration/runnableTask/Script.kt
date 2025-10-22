package org.micoli.php.tasks.configuration.runnableTask

import io.swagger.v3.oas.annotations.media.Schema

class Script : RunnableTaskConfiguration(), TaskWithIcon {
    @Schema(
        description =
            "Source code of the script to execute. Content depends on the language specified by the extension")
    var source: String? = null

    @Schema(
        description =
            "Script language extension used. Default 'groovy', determines the interpreter to use for execution")
    var extension: String = "groovy"

    @Schema(
        description =
            "Path to the icon to display for this script task. Uses standard IntelliJ Platform icons")
    override var icon: String = "debugger/threadRunning.svg"
}
