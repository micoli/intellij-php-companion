package org.micoli.php.notes.models

data class NoteTreeNode(
    val name: String,
    val type: NoteType,
    val fullPath: String,
    val pathId: String? = null,
    val note: Note? = null,
    val children: MutableMap<String, NoteTreeNode> = mutableMapOf()
) {
    fun isLeaf(): Boolean = note != null
}
