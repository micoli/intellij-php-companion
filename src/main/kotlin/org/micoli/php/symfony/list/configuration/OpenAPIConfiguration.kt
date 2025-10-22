package org.micoli.php.symfony.list.configuration

import io.swagger.v3.oas.annotations.media.Schema
import org.micoli.php.configuration.models.DisactivableConfiguration

class OpenAPIConfiguration : DisactivableConfiguration {
    override fun isDisabled(): Boolean {
        return !enabled
    }

    @Schema(description = "Enabler for panel of OAS routes") var enabled: Boolean = true

    @Schema(
        description = "List of root files of swagger/openapi yaml/json files",
        examples = ["public/openapi.yaml", "private/openapi.yaml"])
    var specificationRoots: Array<String> = arrayOf()
}
