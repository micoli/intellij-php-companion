package org.micoli.php.examples

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.swagger.v3.oas.annotations.media.Schema

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = ClassA::class)
@JsonSubTypes(JsonSubTypes.Type(name = "ClassA", value = ClassA::class), JsonSubTypes.Type(name = "ClassB", value = ClassB::class))
abstract class AbstractSubConfiguration {
    @Schema(description = "description of property1", example = "example value of property1") var label: String = "default value of property1"
}
