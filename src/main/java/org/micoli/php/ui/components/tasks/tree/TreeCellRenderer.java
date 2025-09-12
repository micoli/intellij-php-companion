package org.micoli.php.ui.components.tasks.tree;

import com.intellij.ide.util.treeView.NodeRenderer;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;

public class TreeCellRenderer extends NodeRenderer {
    @Override
    public void customizeCellRenderer(
            @NotNull JTree tree,
            Object value,
            boolean selected,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus) {

        if (value instanceof PathNode pathNode) {
            this.append(pathNode.getLabel());
            return;
        }

        if (value instanceof DynamicTreeNode dynamicTreeNode) {
            this.setIcon(dynamicTreeNode.getIcon());
            this.append(dynamicTreeNode.getLabel());
            if (dynamicTreeNode.getTooltip() != null) {
                this.setToolTipText(dynamicTreeNode.getTooltip());
            }
            return;
        }

        super.customizeCellRenderer(tree, value, selected, expanded, leaf, row, hasFocus);
    }
}
