package org.micoli.php.ui.components.tasks.toolbar;

import static com.intellij.openapi.actionSystem.impl.PresentationFactory.updatePresentation;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.messages.MessageBusConnection;
import java.util.Objects;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;
import org.micoli.php.events.TaskNodeChangedEvents;
import org.micoli.php.tasks.TasksService;
import org.micoli.php.tasks.configuration.runnableTask.ObservedFile;
import org.micoli.php.ui.PhpCompanionIcon;

public class FileObserverToolbarButton extends AnAction implements Disposable {
    private static final Logger LOGGER = Logger.getInstance(FileObserverToolbarButton.class.getSimpleName());

    @NotNull
    private final Project project;

    private final @NotNull String taskId;
    private final String label;

    private final @NotNull MessageBusConnection messageBusConnection;

    public FileObserverToolbarButton(Project project, ObservedFile observedFile) {
        super(
                observedFile.label,
                observedFile.label,
                IconLoader.getIcon(observedFile.getIcon(), PhpCompanionIcon.class));
        this.project = project;
        this.label = observedFile.label;
        this.taskId = observedFile.id;
        this.messageBusConnection = project.getMessageBus().connect();
        this.messageBusConnection.subscribe(
                TaskNodeChangedEvents.NODE_CHANGED_EVENTS_TOPIC, getTaskNodeChangedEvents());
    }

    private @NotNull TaskNodeChangedEvents getTaskNodeChangedEvents() {
        return (_taskId, status, iconAndPrefix) -> {
            if (!Objects.equals(_taskId, this.taskId)) {
                return;
            }
            SwingUtilities.invokeLater(() -> {
                LOGGER.warn("Received node changed event for " + taskId + "-" + status.name());
                Presentation presentation = getTemplatePresentation();
                presentation.setText(iconAndPrefix.prefix + label);
                presentation.setIcon(iconAndPrefix.icon);
                updatePresentation(this);
            });
        };
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        TasksService.getInstance(project).runTask(taskId);
    }

    @Override
    public void dispose() {
        messageBusConnection.disconnect();
    }
}
