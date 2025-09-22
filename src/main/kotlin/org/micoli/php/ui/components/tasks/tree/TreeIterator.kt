package org.micoli.php.ui.components.tasks.tree

import com.intellij.ui.treeStructure.Tree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreePath

class TreeIterator private constructor(private val leafProcessor: LeafProcessor) {
    fun interface LeafProcessor {
        fun process(node: DefaultMutableTreeNode?, isLeaf: Boolean, path: TreePath?, level: Int, index: Int)
    }

    private var index: Int = -1

    private fun forEachRecursive(node: DefaultMutableTreeNode, level: Int) {
        index++
        val path = TreePath(node.path)
        if (node.isLeaf) {
            leafProcessor.process(node, true, path, level, index)
            return
        }
        leafProcessor.process(node, false, path, level, index)
        for (nodeIndex in 0..<node.childCount) {
            forEachRecursive((node.getChildAt(nodeIndex) as DefaultMutableTreeNode?)!!, level + 1)
        }
    }

    companion object {
        fun forEach(tree: Tree, leafProcessor: LeafProcessor) {
            TreeIterator(leafProcessor).forEachRecursive((tree.model.root as DefaultMutableTreeNode?)!!, 1)
        }
    }
}
