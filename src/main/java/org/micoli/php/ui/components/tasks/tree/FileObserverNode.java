package org.micoli.php.ui.components.tasks.tree;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.messages.MessageBusConnection;
import java.util.Objects;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.micoli.php.events.TaskNodeChangedEvents;
import org.micoli.php.tasks.configuration.runnableTask.ObservedFile;
import org.micoli.php.ui.PhpCompanionIcon;

public class FileObserverNode extends DynamicTreeNode implements Disposable {
    private static final Logger LOGGER = Logger.getInstance(FileObserverNode.class.getSimpleName());

    private final @NotNull MessageBusConnection messageBusConnection;
    private final @Nullable String initialLabel;

    public FileObserverNode(
            @NotNull Project project, @NotNull Tree tree, @Nullable String label, @NotNull ObservedFile observedFile) {
        super(
                project,
                tree,
                observedFile.id,
                IconLoader.getIcon(observedFile.unknownIcon, PhpCompanionIcon.class),
                label,
                observedFile);
        this.initialLabel = label;
        messageBusConnection = project.getMessageBus().connect();
        messageBusConnection.subscribe(TaskNodeChangedEvents.NODE_CHANGED_EVENTS_TOPIC, getTaskNodeChangedEvents());
    }

    private @NotNull TaskNodeChangedEvents getTaskNodeChangedEvents() {
        return (taskId, status, iconAndPrefix) -> {
            if (!Objects.equals(taskId, this.getTaskId())) {
                return;
            }
            SwingUtilities.invokeLater(() -> {
                setIconAndLabel(iconAndPrefix.icon, iconAndPrefix.prefix + initialLabel);
            });
        };
    }

    @Override
    public void dispose() {
        messageBusConnection.disconnect();
    }
}
