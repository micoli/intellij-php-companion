package org.micoli.php.notes.configuration

import io.swagger.v3.oas.annotations.media.Schema
import org.micoli.php.configuration.models.DisactivableConfiguration

class NotesConfiguration : DisactivableConfiguration {
    override fun isDisabled(): Boolean {
        return !enabled
    }

    @Schema(description = "Enabler for Php Class style configuration", example = "true")
    var enabled: Boolean = true
}
