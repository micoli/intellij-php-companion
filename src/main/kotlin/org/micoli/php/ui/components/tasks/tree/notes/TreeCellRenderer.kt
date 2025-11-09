package org.micoli.php.ui.components.tasks.tree.notes

import com.intellij.icons.AllIcons
import com.intellij.ide.util.treeView.NodeRenderer
import javax.swing.JTree

class TreeCellRenderer : NodeRenderer() {
    override fun customizeCellRenderer(
        tree: JTree,
        value: Any?,
        selected: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean,
    ) {
        if (value is PathNode) {
            this.setIcon(AllIcons.Nodes.Folder)
            this.append(value.getLabel())
            return
        }
        if (value is NoteNode) {
            this.setIcon(value.note.type.getIcon())
            this.append(value.getLabel())
            return
        }

        super.customizeCellRenderer(tree, value, selected, expanded, leaf, row, hasFocus)
    }
}
