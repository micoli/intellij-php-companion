package org.micoli.php.configuration.documentation.sourceCode

data class Parameter(
    val name: String,
    val type: String,
    val defaultValue: String? = null,
    val documentation: String? = null
)
