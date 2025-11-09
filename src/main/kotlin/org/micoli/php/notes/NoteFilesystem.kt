package org.micoli.php.notes

import com.intellij.openapi.project.Project
import java.io.File
import java.util.UUID
import kotlin.collections.forEach
import org.micoli.php.notes.models.Note
import org.micoli.php.notes.models.NoteType
import org.micoli.php.notes.models.Notes
import org.micoli.php.notes.models.Path
import org.micoli.php.notes.models.Paths
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml

class NoteFilesystem(private val project: Project) {

    data class StorageData(val paths: Paths = Paths(), val notes: Notes = Notes())

    private val storageFile: File
        get() = File(project.basePath, ".idea/notes.yaml")

    private val yaml: Yaml

    init {
        val options =
            DumperOptions().apply {
                defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
                isPrettyFlow = true
            }
        yaml = Yaml(options)
    }

    fun getFullPath(pathId: String?): String {
        if (pathId == null) return ""
        val data = loadData()
        return data.paths.buildFullPath(pathId)
    }

    fun allNames(): List<String> {
        val data = loadData()
        val allNames = mutableListOf<String>()
        allNames.addAll(
            data.paths.values.map { path -> data.paths.buildFullPath(path.id) }.toList())
        allNames.addAll(
            data.notes.values
                .map { note ->
                    var folder = data.paths.buildFullPath(note.pathId)
                    if (folder.isNotEmpty()) folder = "${folder}/"
                    "${folder}${note.name}"
                }
                .toList())

        return allNames
    }

    fun exists(fullPath: String): Boolean {
        if (fullPath.isEmpty()) return false

        return allNames().contains(fullPath)
    }

    fun assertPathOrNoteNotExistsByFullPath(fullPath: String) {
        if (exists(fullPath)) {
            throw NotesException("Path or note with full path '$fullPath' already exists")
        }
    }

    fun findPathIdByFullPath(fullPath: String): String? {
        if (fullPath.isEmpty()) return null
        val data = loadData()
        return data.paths.entries.find { (id, _) -> data.paths.buildFullPath(id) == fullPath }?.key
    }

    fun addPath(parentPathId: String?, name: String): String {
        val data = loadData()
        val id = UUID.randomUUID().toString()

        if (parentPathId != null && !data.paths.containsKey(parentPathId)) {
            throw NotesException("Parent path not found")
        }
        assertPathOrNoteNotExistsByFullPath(data.paths.buildFullPath(id, name))

        val path = Path(id = id, label = name, parentId = parentPathId, isExpanded = true)

        data.paths[id] = path
        saveData(data)
        return id
    }

    fun updatePathExpansion(pathId: String, isExpanded: Boolean) {
        val data = loadData()
        val path = data.paths[pathId]
        if (path != null) {
            data.paths[pathId] = path.copy(isExpanded = isExpanded)
            saveData(data)
        }
    }

    fun getPathExpansionState(pathId: String): Boolean {
        val data = loadData()
        return data.paths[pathId]?.isExpanded ?: true
    }

    fun addNote(pathId: String?, name: String, type: NoteType, content: String): Note {
        val data = loadData()
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        if (pathId != null && !data.paths.containsKey(pathId)) {
            throw NotesException("Path not found")
        }
        assertPathOrNoteNotExistsByFullPath(data.paths.buildFullPath(pathId, name))

        val note =
            Note(
                id = id,
                pathId = pathId,
                name = name,
                type = type,
                content = content,
                createdAt = now,
                updatedAt = now)
        data.notes[id] = note
        saveData(data)
        return note
    }

    fun getNoteById(id: String): Note? {
        return loadData().notes[id]
    }

    fun deleteNoteById(id: String) {
        val data = loadData()
        data.notes.remove(id)
        saveData(data)
    }

    fun updateNote(note: Note) {
        val data = loadData()
        data.notes[note.id] = note
        saveData(data)
    }

    fun findNoteByName(name: String): Note? {
        val data = loadData()
        data.notes.values.forEach { note -> if (note.name == name) return note }
        return null
    }

    fun findPathByLabel(label: String, parentId: String?): Path? {
        val data = loadData()
        return data.paths.values.find { it.label == label && it.parentId == parentId }
    }

    fun getChildPaths(parentId: String?): List<Path> {
        val data = loadData()
        return data.paths.values.filter { it.parentId == parentId }
    }

    fun getAllDescendantPathIds(pathId: String): List<String> {
        val data = loadData()
        val descendants = mutableListOf<String>()
        val queue = mutableListOf(pathId)

        while (queue.isNotEmpty()) {
            val currentId = queue.removeAt(0)
            descendants.add(currentId)
            val children = data.paths.values.filter { it.parentId == currentId }
            queue.addAll(children.map { it.id })
        }

        return descendants
    }

    fun deletePathById(id: String) {
        val data = loadData()
        val descendantIds = getAllDescendantPathIds(id)

        descendantIds.forEach { pathId -> data.paths.remove(pathId) }

        data.notes.entries.removeIf { it.value.pathId in descendantIds }

        saveData(data)
    }

    fun renameNoteById(id: String, name: String) {
        val data = loadData()
        val note = data.notes[id] ?: return

        assertPathOrNoteNotExistsByFullPath(data.paths.buildFullPath(note.pathId, name))

        data.notes[id] = note.copy(name = name)
        saveData(data)
    }

    fun renamePathById(id: String, name: String) {
        val data = loadData()
        val path = data.paths[id] ?: return

        assertPathOrNoteNotExistsByFullPath(data.paths.buildFullPath(path.parentId, name))

        data.paths[id] = path.copy(label = name)
        saveData(data)
    }

    fun getPathById(id: String): Path? {
        return loadData().paths[id]
    }

    fun getPathByFullPath(fullPath: String): Path? {
        val data = loadData()
        return data.paths.entries
            .find { (id, _) -> data.paths.buildFullPath(id) == fullPath }
            ?.value
    }

    fun moveNote(noteId: String, newPathId: String?) {
        val data = loadData()
        val note = data.notes[noteId] ?: throw NotesException("Note not found")

        if (newPathId != null && !data.paths.containsKey(newPathId)) {
            throw NotesException("Target path not found")
        }

        val existingNote =
            data.notes.values.find {
                it.name == note.name && it.pathId == newPathId && it.id != noteId
            }
        if (existingNote != null) {
            throw NotesException("Note with name '${note.name}' already exists in target path")
        }

        data.notes[noteId] = note.copy(pathId = newPathId, updatedAt = System.currentTimeMillis())
        saveData(data)
    }

    fun movePath(pathId: String, newParentId: String?) {
        val data = loadData()
        val path = data.paths[pathId] ?: throw NotesException("Path not found")

        if (newParentId != null && !data.paths.containsKey(newParentId)) {
            throw NotesException("Target path not found")
        }

        if (pathId == newParentId) {
            throw NotesException("Cannot move path into itself")
        }

        if (newParentId != null) {
            val descendantIds = getAllDescendantPathIds(pathId)
            if (newParentId in descendantIds) {
                throw NotesException("Cannot move path into its own descendant")
            }
        }

        val existingPath =
            data.paths.values.find {
                it.label == path.label && it.parentId == newParentId && it.id != pathId
            }
        if (existingPath != null) {
            throw NotesException("Path with name '${path.label}' already exists in target location")
        }

        data.paths[pathId] = path.copy(parentId = newParentId)
        saveData(data)
    }

    fun loadData(): StorageData {
        if (!storageFile.exists()) {
            return StorageData()
        }

        try {
            val yamlData =
                yaml.load<Map<String, Any>>(storageFile.readText()) ?: return StorageData()
            val data = StorageData()

            val pathsData = (yamlData["paths"] as? Map<*, *>) ?: emptyMap<Any, Any>()
            pathsData.forEach { (id, pathData) ->
                if (pathData is Map<*, *>) {
                    @Suppress("UNCHECKED_CAST") val pathMap = pathData as Map<String, Any>
                    val idStr = id.toString()

                    data.paths[idStr] =
                        Path(
                            id = idStr,
                            label = pathMap["label"] as? String ?: "",
                            parentId = pathMap["parentId"] as? String,
                            isExpanded = (pathMap["isExpanded"] as? Boolean) ?: true,
                            createdAt =
                                (pathMap["createdAt"] as? Number)?.toLong()
                                    ?: System.currentTimeMillis())
                }
            }

            val notesData = (yamlData["notes"] as? Map<*, *>) ?: emptyMap<Any, Any>()
            notesData.forEach { (id, noteData) ->
                if (noteData is Map<*, *>) {
                    @Suppress("UNCHECKED_CAST") val noteMap = noteData as Map<String, Any>
                    val idStr = id.toString()
                    data.notes[idStr] =
                        Note(
                            id = idStr,
                            pathId = noteMap["pathId"] as? String,
                            name = noteMap["name"] as? String ?: "",
                            type =
                                try {
                                    NoteType.valueOf(
                                        noteMap["type"] as? String ?: NoteType.OTHER.name)
                                } catch (_: IllegalArgumentException) {
                                    NoteType.OTHER
                                },
                            content = noteMap["content"] as? String ?: "",
                            createdAt =
                                (noteMap["createdAt"] as? Number)?.toLong()
                                    ?: System.currentTimeMillis(),
                            updatedAt =
                                (noteMap["updatedAt"] as? Number)?.toLong()
                                    ?: System.currentTimeMillis())
                }
            }

            return data
        } catch (e: Exception) {
            e.printStackTrace()
            return StorageData()
        }
    }

    private fun saveData(data: StorageData) {
        storageFile.parentFile?.mkdirs()

        val pathsData = mutableMapOf<String, Map<String, Any?>>()
        data.paths.forEach { (id, path) ->
            pathsData[id] =
                mutableMapOf<String, Any?>(
                        "label" to path.label,
                        "isExpanded" to path.isExpanded,
                        "createdAt" to path.createdAt)
                    .apply {
                        if (path.parentId != null) {
                            this["parentId"] = path.parentId
                        }
                    }
        }

        val notesData = mutableMapOf<String, Map<String, Any?>>()
        data.notes.forEach { (id, note) ->
            notesData[id] =
                mutableMapOf<String, Any?>(
                        "name" to note.name,
                        "content" to note.content,
                        "type" to note.type.name,
                        "createdAt" to note.createdAt,
                        "updatedAt" to note.updatedAt)
                    .apply {
                        if (note.pathId != null) {
                            this["pathId"] = note.pathId
                        }
                    }
        }

        val yamlData = linkedMapOf("paths" to pathsData, "notes" to notesData)

        storageFile.writeText(yaml.dump(yamlData))
    }
}
