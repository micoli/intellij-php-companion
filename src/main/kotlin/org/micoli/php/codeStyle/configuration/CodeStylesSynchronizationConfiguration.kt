package org.micoli.php.codeStyle.configuration

import io.swagger.v3.oas.annotations.media.Schema
import org.micoli.php.configuration.models.DisactivableConfiguration

class CodeStylesSynchronizationConfiguration : DisactivableConfiguration {
    override fun isDisabled(): Boolean {
        return !enabled
    }

    @Schema(description = "Enabler for panel of Code style synchronization", example = "true")
    var enabled: Boolean = false

    @Schema(description = "Array if code styles to synchronize")
    var styles: Array<CodeStyle> = arrayOf()
}
