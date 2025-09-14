package org.micoli.php.tasks.configuration;

import io.swagger.v3.oas.annotations.media.Schema;

public final class Task extends AbstractNode {
    @Schema(
            description = "Identifier of the referenced task. Must match the ID of an existing task in the"
                    + " TasksConfiguration tasks array",
            example = "aTaskId")
    public String taskId;

    @Schema(description = "If set, it will overide task label in tree", example = "aLabel")
    public String label;
}
