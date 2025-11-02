package org.micoli.php.examples

import io.swagger.v3.oas.annotations.media.Schema

class ClassA : AbstractSubConfiguration() {
    @Schema(description = "description of property1", example = "example value of property1")
    var classADescription: String = "default value of property1"
    @Schema(description = "") var enumProperty: AnEnum = AnEnum.A_VALUE
}
