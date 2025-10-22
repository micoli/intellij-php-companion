package org.micoli.php.tasks.configuration.runnableTask

import io.swagger.v3.oas.annotations.media.Schema

class Link : RunnableTaskConfiguration(), TaskWithIcon {
    @Schema(description = "", example = "http://www.github.com") var url: String? = null

    @Schema(
        description =
            "Path to the icon to display for this link task. Uses standard IntelliJ Platform icons")
    override var icon: String = "expui/gutter/web.svg"
}
