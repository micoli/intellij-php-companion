package org.micoli.php.code.configuration

import io.swagger.v3.oas.annotations.media.Schema
import org.micoli.php.configuration.models.DisactivableConfiguration

class CodeConfiguration : DisactivableConfiguration {
    override fun isDisabled(): Boolean {
        return !enabled
    }

    @Schema(description = "Enabler for panel of Code style synchronization", example = "true")
    var enabled: Boolean = false
}
