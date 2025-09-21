package org.micoli.php.examples

import io.swagger.v3.oas.annotations.media.Schema

class ClassB : AbstractSubConfiguration() {
    @Schema(description = "description of property1", example = "example value of property1") var classBDescription: String = "default value of property1"
}
