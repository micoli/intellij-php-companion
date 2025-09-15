package org.micoli.php.ui.components.tasks.tree;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonShortcuts;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.treeStructure.Tree;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.Objects;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.jetbrains.annotations.NotNull;
import org.micoli.php.tasks.configuration.*;
import org.micoli.php.tasks.configuration.runnableTask.ObservedFile;
import org.micoli.php.tasks.configuration.runnableTask.RunnableTaskConfiguration;
import org.micoli.php.tasks.configuration.runnableTask.Script;
import org.micoli.php.tasks.configuration.runnableTask.Shell;
import org.micoli.php.ui.PhpCompanionIcon;

public class ActionTreeNodeConfigurator {
    protected static final Logger LOGGER = Logger.getInstance(ActionTreeNodeConfigurator.class.getSimpleName());
    private final Tree tree;
    private final DefaultTreeModel treeModel;
    private final DefaultMutableTreeNode root;
    private final Project project;

    public ActionTreeNodeConfigurator(Project project, Tree tree) {
        this.tree = tree;
        this.project = project;
        this.treeModel = (DefaultTreeModel) tree.getModel();
        this.root = (DefaultMutableTreeNode) treeModel.getRoot();
        this.registerDoubleClickAction(tree);
        this.registerEnterKeyAction(tree);
    }

    public void configureTree(Map<String, RunnableTaskConfiguration> runnables, AbstractNode[] nodes) {
        cleanup();
        treeModel.reload();

        addSubNodes(tree, root, runnables, nodes);
        treeModel.reload();
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.repaint();

        TreeIterator.forEach(tree, (node, isLeaf, path, level, index) -> {
            if (!isLeaf && level <= 1) {
                tree.expandRow(index);
            }
        });
    }

    private void addSubNodes(
            Tree tree,
            DefaultMutableTreeNode parent,
            Map<String, RunnableTaskConfiguration> runnables,
            AbstractNode[] nodes) {
        if (nodes == null) {
            return;
        }
        for (AbstractNode node : nodes) {
            switch (node) {
                case Task task -> {
                    RunnableTaskConfiguration runnable = runnables.get(task.taskId);
                    if (runnable == null) {
                        LOGGER.warn("Runnable not found for task ID: " + task.taskId);
                        continue;
                    }

                    parent.add(createTaskNode(tree, task, runnable));
                    treeModel.nodesWereInserted(parent, new int[] {parent.getChildCount() - 1});
                }
                case Path path -> {
                    PathNode treeNode = new PathNode(node, path.label);
                    parent.add(treeNode);
                    treeModel.nodesWereInserted(parent, new int[] {parent.getChildCount() - 1});
                    addSubNodes(tree, treeNode, runnables, path.tasks);
                }
                default -> throw new IllegalStateException("Unexpected value: " + node);
            }
        }
    }

    private @NotNull DefaultMutableTreeNode createTaskNode(Tree tree, Task task, RunnableTaskConfiguration runnable) {
        return switch (runnable) {
            case Shell shell -> new DynamicTreeNode(
                    project,
                    tree,
                    shell.id,
                    IconLoader.getIcon(shell.icon, PhpCompanionIcon.class),
                    Objects.requireNonNullElse(task.label, shell.label),
                    shell);
            case Script script -> new DynamicTreeNode(
                    project,
                    tree,
                    script.id,
                    IconLoader.getIcon(script.icon, PhpCompanionIcon.class),
                    Objects.requireNonNullElse(task.label, script.label),
                    script);
            case ObservedFile observedFile -> new FileObserverNode(
                    project, tree, Objects.requireNonNullElse(task.label, observedFile.label), observedFile);
            default -> throw new IllegalStateException("Unexpected value: " + runnable);
        };
    }

    private void registerDoubleClickAction(Tree tree) {
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() != 2) {
                    return;
                }
                TreePath path = tree.getPathForRow(tree.getClosestRowForLocation(e.getX(), e.getY()));
                if (path == null) {
                    return;
                }
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                handleLeafAction(node, tree);
            }
        });
    }

    private void registerEnterKeyAction(Tree tree) {
        AnAction enterAction = new AnAction() {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                TreePath selectedPath = tree.getSelectionPath();
                if (selectedPath != null) {
                    handleLeafAction((DefaultMutableTreeNode) selectedPath.getLastPathComponent(), tree);
                }
            }
        };

        enterAction.registerCustomShortcutSet(CommonShortcuts.ENTER, tree);
    }

    private void handleLeafAction(DefaultMutableTreeNode node, Tree tree) {
        if (node == null) return;
        if (node instanceof DynamicTreeNode dynamicTreeNode) {
            dynamicTreeNode.run();
            return;
        }

        TreePath path = new TreePath(node.getPath());
        if (tree.isExpanded(path)) {
            tree.collapsePath(path);
            return;
        }
        tree.expandPath(path);
    }

    private void cleanup() {
        TreeIterator.forEach(tree, (node, isLeaf, path, level, index) -> {
            if (node instanceof Disposable disposable) {
                disposable.dispose();
            }
        });
        root.removeAllChildren();
    }
}
