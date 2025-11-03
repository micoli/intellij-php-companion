package org.micoli.php.runner.configuration

import io.swagger.v3.oas.annotations.media.Schema

class PhpRunnerConfiguration {
    @Schema(description = "Path to the PHP interpreter executable") lateinit var bin: String
}
