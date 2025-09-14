package org.micoli.php.tasks.configuration.runnableTask;

import io.swagger.v3.oas.annotations.media.Schema;
import org.micoli.php.tasks.configuration.runnableTask.postToggle.PostToggle;

public final class ObservedFile extends RunnableTaskConfiguration {
    @Schema(
            description =
                    "Prefix used to identify comments in the observed file. Default '#', can be adapted according to"
                            + " file type (e.g. '//' for Java)")
    public String commentPrefix = "#";

    @Schema(
            description =
                    "Path to the file to observe for state changes detection. Can be a project-relative or absolute"
                            + " path")
    public String filePath;

    @Schema(
            description =
                    "Name of the variable or property to monitor in the observed file. Used to determine current state"
                            + " (active/inactive) of the configuration")
    public String variableName;

    @Schema(
            description =
                    "Path to the icon to display when the observed state is active. Uses standard IntelliJ Platform"
                            + " icons")
    public String activeIcon = "actions/inlayRenameInComments.svg";

    @Schema(
            description =
                    "Path to the icon to display when the observed state is inactive. Uses standard IntelliJ Platform"
                            + " icons")
    public String inactiveIcon = "actions/inlayRenameInCommentsActive.svg";

    @Schema(
            description =
                    "Path to the icon to display when the observed state is undetermined or unknown. Uses standard"
                            + " IntelliJ Platform icons")
    public String unknownIcon = "expui/fileTypes/unknown.svg";

    @Schema(
            description =
                    "Optional configuration for an action to execute after state toggle. Can be an action (command) or"
                            + " a script to execute")
    public PostToggle postToggle = null;

    @Override
    public String getIcon() {
        return activeIcon;
    }
}
