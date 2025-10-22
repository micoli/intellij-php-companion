package org.micoli.php.builders

import org.micoli.php.codeStyle.configuration.CodeStyle

class CodeStyleBuilder private constructor() {
    private val codeStyle: CodeStyle = CodeStyle()

    fun withStyleAttribute(attribute: String): CodeStyleBuilder {
        codeStyle.styleAttribute = attribute
        return this
    }

    fun withValue(value: String): CodeStyleBuilder {
        codeStyle.value = value
        return this
    }

    fun build(): CodeStyle = codeStyle

    companion object {
        @JvmStatic fun create(): CodeStyleBuilder = CodeStyleBuilder()
    }
}
