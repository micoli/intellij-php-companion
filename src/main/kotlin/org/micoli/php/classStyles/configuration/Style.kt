package org.micoli.php.classStyles.configuration

import io.swagger.v3.oas.annotations.media.Schema
import org.micoli.php.configuration.schema.EnumArraySchema
import org.micoli.php.configuration.schema.ValidEnumArray

class Style {
    @Schema(description = "Foreground color of the class") var foregroundColor: FontColor? = null
    @Schema(description = "Background color of the class") var backgroundColor: FontColor? = null

    @ValidEnumArray
    @EnumArraySchema(enumClass = FontStyle::class)
    @Schema(description = "Font style/variant")
    var fontStyles: Set<FontStyle> = setOf()
}
