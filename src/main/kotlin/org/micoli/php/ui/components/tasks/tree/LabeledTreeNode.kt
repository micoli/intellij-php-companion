package org.micoli.php.ui.components.tasks.tree

import javax.swing.tree.DefaultMutableTreeNode

abstract class LabeledTreeNode : DefaultMutableTreeNode {

    private var innerLabel: String

    constructor(userObject: Any?, label: String) : super(userObject) {
        this.innerLabel = label
    }

    protected fun setLabel(label: String) {
        this.innerLabel = label
    }

    fun getLabel(): String {
        return this.innerLabel
    }

    fun getTooltip(): String {
        return this.innerLabel
    }
}
