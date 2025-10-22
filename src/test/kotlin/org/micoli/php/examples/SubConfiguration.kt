package org.micoli.php.examples

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "description of SubConfiguration")
class SubConfiguration {
    @Schema(description = "description of property1", example = "example value of property1")
    var aProperty1: String = "default value of property1"

    var aProperty2: String? = null
}
