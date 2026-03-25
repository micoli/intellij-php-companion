package org.micoli.php.ui.components.tasks.tree.notes

import javax.swing.tree.DefaultMutableTreeNode
import org.micoli.php.notes.models.NoteTreeNode

class PathNode(val noteTreeNode: NoteTreeNode, private val label: String) :
    DefaultMutableTreeNode(label) {

    fun getLabel(): String = label

    override fun toString(): String = label

    override fun isLeaf(): Boolean = false
}
