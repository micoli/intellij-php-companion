package org.micoli.php.tasks.configuration.runnableTask

import io.swagger.v3.oas.annotations.media.Schema
import org.micoli.php.tasks.configuration.runnableTask.postToggle.PostToggle

class ObservedFile(override val icon: String? = null) : RunnableTaskConfiguration() {
    @Schema(
        description =
            "Prefix used to identify comments in the observed file. Default '#', can be adapted according to file type (e.g. '//' for Java)")
    var commentPrefix: String = "#"

    @Schema(
        description =
            "Path to the file to observe for state changes detection. Can be a project-relative or absolute path")
    var filePath: String? = null

    @Schema(
        description =
            "Name of the variable or property to monitor in the observed file. Used to determine current state (active/inactive) of the configuration")
    var variableName: String? = null

    @Schema(
        description =
            "Path to the icon to display when the observed state is active. Uses standard IntelliJ Platform icons")
    var activeIcon: String = "actions/inlayRenameInComments.svg"

    @Schema(
        description =
            "Path to the icon to display when the observed state is inactive. Uses standard IntelliJ Platform icons")
    var inactiveIcon: String = "actions/inlayRenameInCommentsActive.svg"

    @Schema(
        description =
            "Path to the icon to display when the observed state is undetermined or unknown. Uses standard IntelliJ Platform icons")
    var unknownIcon: String = "expui/fileTypes/unknown.svg"

    @Schema(
        description =
            "Optional configuration for an action to execute after state toggle. Can be an action (command) or a script to execute")
    var postToggle: PostToggle? = null
}
