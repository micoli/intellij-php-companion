package org.micoli.php.tasks.configuration.runnableTask;

import io.swagger.v3.oas.annotations.media.Schema;

public final class Script extends RunnableTaskConfiguration {
    @Schema(
            description =
                    "Source code of the script to execute. Content depends on the language specified by the extension")
    public String source = null;

    @Schema(
            description =
                    "Script language extension used. Default 'groovy', determines the interpreter to use for execution")
    public String extension = "groovy";

    @Schema(description = "Path to the icon to display for this script task. Uses standard IntelliJ Platform icons")
    public String icon = "debugger/threadRunning.svg";

    @Override
    public String getIcon() {
        return icon;
    }
}
