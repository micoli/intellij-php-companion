package org.micoli.php.notes.models

data class Path(
    val id: String,
    val label: String,
    val parentId: String? = null,
    val isExpanded: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
