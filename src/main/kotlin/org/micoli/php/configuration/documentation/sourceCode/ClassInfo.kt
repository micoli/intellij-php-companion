package org.micoli.php.configuration.documentation.sourceCode

data class ClassInfo(
    val name: String,
    val type: String, // "class", "interface", "data class", "object", etc.
    val methods: List<MethodInfo>,
    val properties: List<String> = emptyList(),
    val documentation: String? = null
)
