package org.micoli.php.tasks.configuration

import io.swagger.v3.oas.annotations.media.Schema

class Path : AbstractNode() {
    @Schema(
        description =
            ("Label displayed for this folder in the hierarchical tree. User-friendly name for organizing tasks" +
                " into logical groups"))
    var label: String? = null

    @Schema(
        description =
            ("Array of child nodes contained in this folder. Can contain other folders (Path) or task" +
                " references (Task)"))
    var tasks: Array<AbstractNode> = arrayOf()
}
