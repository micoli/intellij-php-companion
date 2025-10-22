package org.micoli.php.symfony.list.configuration

import io.swagger.v3.oas.annotations.media.Schema
import org.micoli.php.configuration.models.DisactivableConfiguration

class RoutesConfiguration : DisactivableConfiguration {
    override fun isDisabled(): Boolean {
        return !enabled
    }

    @Schema(description = "Enabler for panel of routes") var enabled: Boolean = true

    @Schema(description = "List of namespaces where routes are searched")
    var namespaces: Array<String> = arrayOf("\\App", "\\Application")

    @Schema(description = "Attribute used to detect routes")
    var attributeFQCN: String = "\\Symfony\\Component\\Routing\\Attribute\\Route"
}
