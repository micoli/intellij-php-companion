package org.micoli.php.configuration.documentation.sourceCode

data class MethodInfo(
    val name: String,
    val parameters: List<Parameter>,
    val returnType: String? = null,
    val isConstructor: Boolean = false,
    val documentation: String? = null
)
