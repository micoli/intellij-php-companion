package org.micoli.php.tasks.configuration.runnableTask.postToggle;

import io.swagger.v3.oas.annotations.media.Schema;

public final class PostToggleShell extends PostToggle {
    @Schema(
            description = "System command to execute in shell. Can include arguments and use environment variables",
            example = "make clear-cache")
    public String command = null;

    @Schema(description = "Current working directory for command execution. If null, uses project root directory")
    public String cwd = null;

    @Schema(description = "Path to the icon to display for this shell task. Uses standard IntelliJ Platform icons")
    public String icon = "debugger/threadRunning.svg";

    @Override
    public String getIcon() {
        return icon;
    }
}
