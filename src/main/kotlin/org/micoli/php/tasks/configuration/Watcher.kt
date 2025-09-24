package org.micoli.php.tasks.configuration

import io.swagger.v3.oas.annotations.media.Schema

class Watcher {
    @Schema(
        description =
            ("Identifier of the task to execute when watched files are modified. Must match the ID of an existing task in the configuration"))
    var taskId: String? = null

    @Schema(
        description =
            ("Delay in milliseconds before task triggering after change detection. Prevents multiple executions during rapid successive modifications"))
    var debounce: Int = 1000

    @Schema(
        description =
            ("Indicates if a notification should be displayed to the user upon triggering. False by default to avoid too frequent notifications"))
    var notify: Boolean = false

    @Schema(
        description =
            "Array of file patterns to watch. Supports wildcards and regular expressions to match file paths")
    var watches: Array<String> = arrayOf()
}
