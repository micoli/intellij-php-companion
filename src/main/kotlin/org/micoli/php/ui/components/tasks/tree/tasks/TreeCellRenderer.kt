package org.micoli.php.ui.components.tasks.tree.tasks

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
            this.append(value.getLabel())
            return
        }

        if (value is DynamicTreeNode) {
            this.setIcon(value.icon)
            this.append(value.getLabel())
            this.setToolTipText(value.getTooltip())
            return
        }

        super.customizeCellRenderer(tree, value, selected, expanded, leaf, row, hasFocus)
    }
}
