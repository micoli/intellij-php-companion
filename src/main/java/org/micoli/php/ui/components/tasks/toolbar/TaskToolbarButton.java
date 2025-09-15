package org.micoli.php.ui.components.tasks.toolbar;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;
import org.micoli.php.tasks.TasksService;
import org.micoli.php.tasks.configuration.runnableTask.RunnableTaskConfiguration;
import org.micoli.php.ui.PhpCompanionIcon;

public class TaskToolbarButton extends AnAction {
    private final Project project;
    private final String taskId;

    public TaskToolbarButton(Project project, RunnableTaskConfiguration action) {
        super(action.label, action.label, IconLoader.getIcon(action.getIcon(), PhpCompanionIcon.class));
        this.project = project;
        this.taskId = action.id;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        TasksService.getInstance(project).runTask(taskId);
    }
}
