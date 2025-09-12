package org.micoli.php.ui.components.tasks.tree;

import com.intellij.ui.treeStructure.Tree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

public class TreeIterator {
    @FunctionalInterface
    public interface LeafProcessor {
        void process(DefaultMutableTreeNode node, boolean isLeaf, TreePath path, int level, int index);
    }

    private final LeafProcessor leafProcessor;
    private int index;

    private TreeIterator(LeafProcessor leafProcessor) {
        this.leafProcessor = leafProcessor;
        this.index = -1;
    }

    public static void forEach(Tree tree, LeafProcessor leafProcessor) {
        new TreeIterator(leafProcessor)
                .forEachRecursive((DefaultMutableTreeNode) tree.getModel().getRoot(), 1);
    }

    private void forEachRecursive(DefaultMutableTreeNode node, int level) {
        index++;
        TreePath path = new TreePath(node.getPath());
        if (node.isLeaf()) {
            leafProcessor.process(node, true, path, level, index);
            return;
        }
        leafProcessor.process(node, false, path, level, index);
        for (int nodeIndex = 0; nodeIndex < node.getChildCount(); nodeIndex++) {
            forEachRecursive((DefaultMutableTreeNode) node.getChildAt(nodeIndex), level + 1);
        }
    }
}
