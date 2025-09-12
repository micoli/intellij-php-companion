package org.micoli.php.tasks.configuration.runnableTask;

import io.swagger.v3.oas.annotations.media.Schema;

public final class PostToggleScript extends PostToggle {
    @Schema(
            description =
                    "Source code of the script to execute after state toggle. Content depends on the language specified by the extension")
    public String source = null;

    @Schema(
            description =
                    "Script language extension used. Default 'groovy', determines the interpreter to use for execution")
    public String extension = "groovy";

    @Override
    public String getIcon() {
        return "";
    }
}
