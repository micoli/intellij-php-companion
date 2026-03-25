package org.micoli.php.ui.components.tasks.tree.notes

import com.intellij.openapi.project.Project
import com.intellij.ui.treeStructure.Tree
import javax.swing.tree.DefaultMutableTreeNode
import org.micoli.php.notes.NotesService
import org.micoli.php.notes.models.Note

class NoteNode(private val project: Project, private val tree: Tree, var note: Note) :
    DefaultMutableTreeNode(note.name) {

    fun openNote() {
        NotesService.getInstance(project).openNote(note) { note = it }
    }

    fun getLabel(): String = note.name

    override fun toString(): String {
        return note.name
    }

    override fun isLeaf(): Boolean = true
}
