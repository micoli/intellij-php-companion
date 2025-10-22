package org.micoli.php.codeStyle.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

class CodeStyle {
    @JsonProperty(required = true)
    @Schema(
        description =
            "Code style field property as in com.intellij.psi.codeStyle.CommonCodeStyleSettings",
        example = "ALIGN_MULTILINE_PARAMETERS_IN_CALLS")
    lateinit var styleAttribute: String

    @JsonProperty(required = true)
    @Schema(description = "a boolean value true/false or an int value", example = "false")
    lateinit var value: String

    @JsonIgnore
    fun isFullyInitialized(): Boolean {
        return ::styleAttribute.isInitialized && ::value.isInitialized
    }
}
