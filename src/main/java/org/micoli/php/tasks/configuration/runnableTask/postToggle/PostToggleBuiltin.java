package org.micoli.php.tasks.configuration.runnableTask.postToggle;

import io.swagger.v3.oas.annotations.media.Schema;

public final class PostToggleBuiltin extends PostToggle {
    @Schema(description = "Builtin actionId to execute", example = "$Copy")
    public String actionId = null;

    @Schema(description = "Path to the icon to display for this builtin task. Uses standard IntelliJ Platform icons")
    public String icon = "debugger/threadRunning.svg";

    @Override
    public String getIcon() {
        return icon;
    }
}
