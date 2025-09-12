package org.micoli.php.ui.components.tasks.tree;

import javax.swing.tree.DefaultMutableTreeNode;

public abstract class LabeledTreeNode extends DefaultMutableTreeNode {

    private String label;

    public LabeledTreeNode(Object userObject, String label) {
        super(userObject, true);
        this.label = label;
    }

    protected void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return this.label;
    }

    public String getTooltip() {
        return this.label;
    }
}
