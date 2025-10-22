package org.micoli.php.symfony.profiler.configuration

import io.swagger.v3.oas.annotations.media.Schema
import org.micoli.php.configuration.models.DisactivableConfiguration

class SymfonyProfilerConfiguration : DisactivableConfiguration {
    override fun isDisabled(): Boolean {
        return !enabled
    }

    @Schema(description = "Enabler for panel of SymfonyProfilers") var enabled: Boolean = false

    @Schema(description = "Local path to Symfony Profiler dumps")
    var profilerPath: String = "var/cache/dev/profiler"

    @Schema(description = "Profiler URL root")
    var profilerUrlRoot: String = "https://127.0.0.1:8000/_profiler/"

    @Schema(description = "List of URL roots of symfony profiles")
    var urlRoots: Array<String> = arrayOf("https://127.0.0.1:8000")

    @Schema(description = "List of regular expression used to filter URI in profilers")
    var excludeFilter: Array<String> = arrayOf()
}
