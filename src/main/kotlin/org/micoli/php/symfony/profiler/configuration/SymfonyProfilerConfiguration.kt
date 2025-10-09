package org.micoli.php.symfony.profiler.configuration

import io.swagger.v3.oas.annotations.media.Schema
import org.micoli.php.configuration.models.DisactivableConfiguration

class SymfonyProfilerConfiguration : DisactivableConfiguration {
    override fun isDisabled(): Boolean {
        return !enabled
    }

    @Schema(description = "Enabler for panel of SymfonyProfilers") var enabled: Boolean = false

    var profilerPath: String = "var/cache/dev/profiler"
    var profilerUrlRoot: String = "https://127.0.0.1:8000/_profiler/"
    var urlRoots: Array<String> = arrayOf("https://127.0.0.1:8000")
}
