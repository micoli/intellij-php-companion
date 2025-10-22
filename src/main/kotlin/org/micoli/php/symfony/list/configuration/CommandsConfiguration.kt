package org.micoli.php.symfony.list.configuration

import io.swagger.v3.oas.annotations.media.Schema
import org.micoli.php.configuration.models.DisactivableConfiguration

class CommandsConfiguration : DisactivableConfiguration {
    override fun isDisabled(): Boolean {
        return !enabled
    }

    @Schema(description = "Enabler for panel of console commands") var enabled: Boolean = true

    @Schema(description = "List of namespaces where console commands are searched")
    var namespaces: Array<String> = arrayOf("\\App", "\\Application")

    @Schema(description = "Attribute used to detect console commands")
    var attributeFQCN: String = "\\Symfony\\Component\\Console\\Attribute\\AsCommand"
}
