package org.micoli.php.symfony.list.configuration

import io.swagger.v3.oas.annotations.media.Schema
import org.micoli.php.configuration.models.DisactivableConfiguration

class DoctrineEntitiesConfiguration : DisactivableConfiguration {
    override fun isDisabled(): Boolean {
        return !enabled
    }

    @Schema(description = "Enabler for panel of doctrine entities") var enabled: Boolean = true

    @Schema(description = "List of namespaces where doctrine entities are searched")
    var namespaces: Array<String> = arrayOf("\\Domain", "\\Entity")

    @Schema(description = "Attribute used to detect Entities")
    var attributeFQCN: String? = "\\Doctrine\\ORM\\Mapping\\Table"
}
