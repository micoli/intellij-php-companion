package org.micoli.php.notes.models

class Paths : HashMap<String, Path>(emptyMap()) {
    fun buildFullPath(pathId: String?, suffix: String): String {
        val path = buildFullPath(pathId)
        if (path == "") return suffix
        return "$path/$suffix"
    }

    fun buildFullPath(pathId: String?): String {
        if (pathId == null) return ""
        val path = this[pathId] ?: return ""
        val parentPath =
            if (path.parentId != null) {
                buildFullPath(path.parentId)
            } else {
                ""
            }
        return if (parentPath.isEmpty()) path.label else "$parentPath/${path.label}"
    }
}
