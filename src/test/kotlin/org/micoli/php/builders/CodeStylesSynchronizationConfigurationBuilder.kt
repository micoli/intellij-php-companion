package org.micoli.php.builders

import org.micoli.php.codeStyle.configuration.CodeStyle
import org.micoli.php.codeStyle.configuration.CodeStylesSynchronizationConfiguration

class CodeStylesSynchronizationConfigurationBuilder private constructor() {
    private val codeStylesSynchronizationConfiguration: CodeStylesSynchronizationConfiguration =
        CodeStylesSynchronizationConfiguration()

    fun withCodeStyles(styles: Array<CodeStyle>): CodeStylesSynchronizationConfigurationBuilder {
        codeStylesSynchronizationConfiguration.styles = styles
        return this
    }

    fun withAddedCodeStyle(codeStyle: CodeStyle): CodeStylesSynchronizationConfigurationBuilder {
        codeStylesSynchronizationConfiguration.styles =
            appendToArray(
                codeStylesSynchronizationConfiguration.styles, codeStyle, CodeStyle::class.java)
        return this
    }

    fun withEnabled(enabled: Boolean): CodeStylesSynchronizationConfigurationBuilder {
        codeStylesSynchronizationConfiguration.enabled = enabled
        return this
    }

    fun build(): CodeStylesSynchronizationConfiguration = codeStylesSynchronizationConfiguration

    companion object {
        @JvmStatic
        fun create(): CodeStylesSynchronizationConfigurationBuilder =
            CodeStylesSynchronizationConfigurationBuilder()
    }
}
