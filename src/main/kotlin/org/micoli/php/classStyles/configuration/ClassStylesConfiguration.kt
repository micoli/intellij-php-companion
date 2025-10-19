package org.micoli.php.classStyles.configuration

import io.swagger.v3.oas.annotations.media.Schema
import org.micoli.php.configuration.models.DisactivableConfiguration

class ClassStylesConfiguration : DisactivableConfiguration {
    override fun isDisabled(): Boolean {
        return !enabled
    }

    @Schema(description = "Enabler for Php Class style configuration", example = "true")
    var enabled: Boolean = false
    var rules: Array<Rule> = arrayOf()
}
