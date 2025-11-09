package org.micoli.php.notes.models

data class Note(
    val id: String,
    val pathId: String?,
    val name: String,
    val type: NoteType,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
