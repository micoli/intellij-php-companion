package org.micoli.php.notes

import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.testFramework.LightVirtualFile
import org.micoli.php.notes.configuration.NotesConfiguration
import org.micoli.php.notes.models.Note
import org.micoli.php.notes.models.NoteTreeNode
import org.micoli.php.notes.models.NoteType
import org.micoli.php.notes.models.NotesTree
import org.micoli.php.notes.models.Path

@Service(Service.Level.PROJECT)
class NotesService(val project: Project) {
    val noteFilesystem = NoteFilesystem(project)

    fun loadConfiguration(noteNavigation: NotesConfiguration?) {
        if (noteNavigation == null) {
            return
        }
    }

    fun getAllNotes(): NotesTree {
        val data = noteFilesystem.loadData()
        val root = NoteTreeNode(name = "", type = NoteType.PATH, fullPath = "", pathId = null)

        val pathIdToNode = mutableMapOf<String, NoteTreeNode>()

        data.paths.values
            .filter { it.parentId == null }
            .forEach { path -> addPathToTreeRecursive(root, path, data.paths, pathIdToNode) }

        data.notes.values.forEach { note -> addNoteToTree(root, note, pathIdToNode) }

        return NotesTree(root)
    }

    private fun addPathToTreeRecursive(
        parent: NoteTreeNode,
        path: Path,
        allPaths: Map<String, Path>,
        pathIdToNode: MutableMap<String, NoteTreeNode>
    ) {
        val fullPath = noteFilesystem.getFullPath(path.id)
        val node =
            NoteTreeNode(
                name = path.label, type = NoteType.PATH, fullPath = fullPath, pathId = path.id)

        parent.children[path.label] = node
        pathIdToNode[path.id] = node

        allPaths.values
            .filter { it.parentId == path.id }
            .forEach { childPath ->
                addPathToTreeRecursive(node, childPath, allPaths, pathIdToNode)
            }
    }

    private fun addNoteToTree(
        root: NoteTreeNode,
        note: Note,
        pathIdToNode: Map<String, NoteTreeNode>
    ) {
        val parent =
            if (note.pathId != null) {
                pathIdToNode[note.pathId] ?: root
            } else {
                root
            }

        val fullPath = noteFilesystem.getFullPath(note.pathId)
        val noteFullPath = if (fullPath.isEmpty()) note.name else "$fullPath/${note.name}"

        parent.children[note.name] =
            NoteTreeNode(
                name = note.name,
                type = note.type,
                fullPath = noteFullPath,
                pathId = note.pathId,
                note = note)
    }

    fun openNote(note: Note, updateCallback: (updatedNote: Note) -> Unit) {
        val fileType = FileTypeManager.getInstance().getFileTypeByExtension(note.type.name)
        val fileEditorManager = FileEditorManager.getInstance(project)

        val existingFile =
            fileEditorManager.openFiles.find { it.getUserData(NOTE_ID_KEY) == note.id }

        if (existingFile != null) {
            fileEditorManager.openFile(existingFile, true)
            return
        }

        val virtualFile = LightVirtualFile(note.name, fileType, note.content)
        virtualFile.isWritable = true
        virtualFile.putUserData(NOTE_ID_KEY, note.id)
        virtualFile.putUserData(NOTE_TYPE_KEY, note.type.name)

        fileEditorManager.openFile(virtualFile, true)
        val document = FileDocumentManager.getInstance().getDocument(virtualFile)
        document?.addDocumentListener(
            object : DocumentListener {
                override fun documentChanged(event: DocumentEvent) {
                    val updatedNote =
                        note.copy(content = document.text, updatedAt = System.currentTimeMillis())
                    noteFilesystem.updateNote(updatedNote)
                    updateCallback(updatedNote)
                }
            })
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): NotesService {
            return project.getService(NotesService::class.java)
        }

        private val NOTE_ID_KEY = Key.create<String>("NOTE_ID")
        private val NOTE_TYPE_KEY = Key.create<String>("NOTE_TYPE")
    }
}
