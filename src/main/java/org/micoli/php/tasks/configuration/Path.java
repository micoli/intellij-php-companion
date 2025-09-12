package org.micoli.php.tasks.configuration;

import io.swagger.v3.oas.annotations.media.Schema;

public final class Path extends AbstractNode {
    @Schema(
            description =
                    "Label displayed for this folder in the hierarchical tree. User-friendly name for organizing tasks into logical groups")
    public String label;

    @Schema(
            description =
                    "Array of child nodes contained in this folder. Can contain other folders (Path) or task references (Task)")
    public AbstractNode[] tasks = new AbstractNode[] {};
}
