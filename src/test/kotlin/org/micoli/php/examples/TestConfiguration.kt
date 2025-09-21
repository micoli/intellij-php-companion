package org.micoli.php.examples

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "description of TestConfiguration")
class TestConfiguration {
    var aSubConfiguration: Array<SubConfiguration?> = arrayOf()

    var aSubSubConfiguration: Array<AbstractSubConfiguration?> = arrayOf()

    @Schema(description = "description of property1", example = "example value of property1") var aBooleanValue: Boolean = false
}
