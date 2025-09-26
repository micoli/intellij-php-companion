package org.micoli.php.consoleCleaner.configuration

import io.swagger.v3.oas.annotations.media.Schema

class ConsoleCleanerConfiguration {
    @Schema(
        description =
            ("Regular expression pattern for parsing output (if pattern start with ^and finished with $, then" +
                " the whole line is stripped out)"))
    var patterns: Array<String> = arrayOf()
}
