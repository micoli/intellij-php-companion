package org.micoli.php.ui.components.tasks.tree;

import com.intellij.openapi.project.Project;
import com.intellij.ui.treeStructure.Tree;
import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.micoli.php.tasks.TasksService;
import org.micoli.php.tasks.configuration.runnableTask.RunnableTaskConfiguration;

public class DynamicTreeNode extends LabeledTreeNode {
    private @NotNull final Project project;
    private @NotNull final Tree tree;
    private @NotNull final String taskId;
    private Icon icon;

    public DynamicTreeNode(
            @NotNull Project project,
            @NotNull Tree tree,
            @NotNull String taskId,
            @Nullable Icon icon,
            @Nullable String label,
            @NotNull RunnableTaskConfiguration configurationNode) {
        super(configurationNode, label);
        this.project = project;
        this.tree = tree;
        this.taskId = taskId;
        this.icon = icon;
    }

    protected void setIconAndLabel(Icon icon, String label) {
        this.icon = icon;
        setLabel(label);
        ((DefaultTreeModel) tree.getModel()).reload(this);
    }

    public Icon getIcon() {
        return icon;
    }

    public void run() {
        TasksService.getInstance(project).runTask(taskId);
    }

    protected @NotNull String getTaskId() {
        return taskId;
    }
}
