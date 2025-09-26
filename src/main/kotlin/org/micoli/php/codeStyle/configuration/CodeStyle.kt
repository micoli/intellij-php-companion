package org.micoli.php.codeStyle.configuration

import io.swagger.v3.oas.annotations.media.Schema

class CodeStyle {
    @Schema(
        description =
            "Code style field property as in com.intellij.psi.codeStyle.CommonCodeStyleSettings",
        example = "ALIGN_MULTILINE_PARAMETERS_IN_CALLS")
    @JvmField
    var styleAttribute: String? = null

    @Schema(description = "a boolean value true/false or an int value", example = "false")
    @JvmField
    var value: String? = null
}
