package org.micoli.php.tasks.configuration.runnableTask;

import io.swagger.v3.oas.annotations.media.Schema;

public final class PostToggleAction extends PostToggle {
    @Schema(
            description =
                    "System command to execute after state toggle. Can include arguments and use environment variables")
    public String command = null;

    @Schema(description = "Current working directory for command execution. If null, uses project root directory")
    public String cwd = null;

    @Override
    public String getIcon() {
        return "";
    }
}
