package org.micoli.php.classStyles.configuration

import com.intellij.openapi.editor.markup.EffectType
import io.swagger.v3.oas.annotations.media.Schema
import org.micoli.php.configuration.schema.EnumArraySchema
import org.micoli.php.configuration.schema.ValidEnumArray

class Style {
    @Schema(description = "Foreground color of the class", example = "BLUE")
    var foregroundColor: FontColor? = null
    @Schema(description = "Background color of the class", example = "BLACK")
    var backgroundColor: FontColor? = null

    @ValidEnumArray
    @EnumArraySchema(enumClass = FontStyle::class)
    @Schema(description = "Font style/variant", example = "[BOLD]")
    var fontStyles: Set<FontStyle> = setOf()
    @Schema(description = "Font effect", example = "LINE_UNDERSCORE") var effect: EffectType? = null
    @Schema(description = "Font effect color", example = "RED") var effectColor: FontColor? = null
}
