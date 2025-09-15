package org.micoli.php.tasks;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.micoli.php.events.TaskNodeChangedEvents;
import org.micoli.php.service.DebouncedRunnables;
import org.micoli.php.service.filesystem.FileListener;
import org.micoli.php.tasks.configuration.TasksConfiguration;
import org.micoli.php.tasks.configuration.Watcher;
import org.micoli.php.tasks.configuration.runnableTask.*;
import org.micoli.php.tasks.configuration.runnableTask.postToggle.PostToggleBuiltin;
import org.micoli.php.tasks.configuration.runnableTask.postToggle.PostToggleScript;
import org.micoli.php.tasks.configuration.runnableTask.postToggle.PostToggleShell;
import org.micoli.php.tasks.models.TaskIdentifier;
import org.micoli.php.tasks.runnables.FileObserverTask;
import org.micoli.php.tasks.runnables.RunnableTask;
import org.micoli.php.ui.Notification;

public class TasksService implements FileListener.VfsHandler<TaskIdentifier> {

    private @NotNull final Project project;
    private @NotNull final FileListener<TaskIdentifier> fileListener;
    protected Map<String, RunnableTask> runnableActions;
    protected DebouncedRunnables debouncedRunnables = new DebouncedRunnables();
    private boolean watchersAreEnabled = true;

    public TasksService(Project project) {
        this.project = project;
        this.fileListener = new FileListener<>(this);
        project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, this.fileListener.getVfsListener());
    }

    public static TasksService getInstance(Project project) {
        return project.getService(TasksService.class);
    }

    public void loadConfiguration(TasksConfiguration tasksConfiguration) {
        fileListener.reset();
        debouncedRunnables.reset();
        if (tasksConfiguration == null) {
            return;
        }

        tasksConfiguration.assertConfigurationIsValid();
        initializeRunnableActions(tasksConfiguration);

        Map<TaskIdentifier, List<PathMatcher>> pathMatcherMap = new HashMap<>();
        pathMatcherMap.putAll(getWatchedFilesFromWatcher(tasksConfiguration));
        pathMatcherMap.putAll(getWatchedFilesFromObservedFiles(tasksConfiguration));
        fileListener.setPatterns(pathMatcherMap);
        refreshObservedFiles(false);
    }

    public void runTask(String taskId) {
        RunnableTask runnableAction = runnableActions.get(taskId);
        runnableAction.run();
        if (runnableAction instanceof FileObserverTask fileObserverTask) {
            updateFileObserver(fileObserverTask, true);
        }
    }

    public void refreshObservedFiles(boolean forceUpdateAll) {
        if (this.runnableActions == null || this.runnableActions.isEmpty()) {
            return;
        }
        for (Map.Entry<String, RunnableTask> taskConfiguration : runnableActions.entrySet()) {
            if (taskConfiguration.getValue() instanceof FileObserverTask fileObserverTask) {
                updateFileObserver(fileObserverTask, forceUpdateAll);
            }
        }
    }

    private static Map<TaskIdentifier, List<PathMatcher>> getWatchedFilesFromObservedFiles(
            TasksConfiguration tasksConfiguration) {
        if (tasksConfiguration.tasks == null) {
            return new HashMap<>();
        }

        return Arrays.stream(tasksConfiguration.tasks)
                .filter(ObservedFile.class::isInstance)
                .map(ObservedFile.class::cast)
                .collect(Collectors.toMap(
                        observedFile -> new TaskIdentifier(observedFile.id, observedFile),
                        observedFile ->
                                List.of(FileSystems.getDefault().getPathMatcher("glob:" + observedFile.filePath))));
    }

    private static Map<TaskIdentifier, List<PathMatcher>> getWatchedFilesFromWatcher(
            TasksConfiguration tasksConfiguration) {
        if (tasksConfiguration.watchers == null) {
            return new HashMap<>();
        }

        return Arrays.stream(tasksConfiguration.watchers)
                .collect(Collectors.toMap(
                        watcher -> new TaskIdentifier(watcher.taskId, watcher),
                        watcher -> Arrays.stream(watcher.watches)
                                .map(FileSystems.getDefault()::getPathMatcher)
                                .toList()));
    }

    private void initializeRunnableActions(TasksConfiguration tasksConfiguration) {
        if (tasksConfiguration.tasks == null) {
            return;
        }
        runnableActions = Stream.of(tasksConfiguration.tasks)
                .filter((RunnableTaskConfiguration task) -> !task.id.isEmpty())
                .collect(Collectors.toMap(
                        task -> task.id,
                        task -> switch (task) {
                            case ObservedFile observedFile -> new FileObserverTask(project, observedFile);
                            case Builtin builtin -> new RunnableTask(project, builtin);
                            case Shell shell -> new RunnableTask(project, shell);
                            case Script script -> new RunnableTask(project, script);
                            case PostToggleBuiltin builtin -> new RunnableTask(project, builtin);
                            case PostToggleShell shell -> new RunnableTask(project, shell);
                            case PostToggleScript script -> new RunnableTask(project, script);
                            default -> throw new IllegalStateException("Unexpected value: " + task);
                        },
                        (existing, replacement) -> replacement));
    }

    @Override
    public void vfsHandle(@NotNull TaskIdentifier taskIdentifier, @NotNull VirtualFile file) {
        RunnableTask runnableTask = runnableActions.get(taskIdentifier.taskId());
        if (runnableTask == null) {
            return;
        }
        if (runnableTask instanceof FileObserverTask fileObserverTask) {
            updateFileObserver(fileObserverTask, true);
            return;
        }
        if (taskIdentifier.configuration() instanceof Watcher watcherConfiguration) {
            if (!watchersAreEnabled) {
                return;
            }
            this.debouncedRunnables.run(runnableTask, taskIdentifier.taskId(), watcherConfiguration.debounce, () -> {
                if (!watcherConfiguration.notify) {
                    return;
                }
                Notification.messageWithTimeout(String.format("%s done", watcherConfiguration.taskId), 800);
            });
            return;
        }
        runnableTask.run();
    }

    protected void updateFileObserver(FileObserverTask fileObserverTask, boolean force) {
        if (!(force || fileObserverTask.hasChanged())) {
            return;
        }
        project.getMessageBus()
                .syncPublisher(TaskNodeChangedEvents.NODE_CHANGED_EVENTS_TOPIC)
                .setNodeChangedEventsTopic(
                        fileObserverTask.getTaskId(),
                        fileObserverTask.getStatus(),
                        fileObserverTask.getIconAndPrefix());
    }

    public boolean isWatcherEnabled() {
        return watchersAreEnabled;
    }

    public void toggleWatcherEnabled() {
        watchersAreEnabled = !watchersAreEnabled;
    }
}

/*
        if (configurationNode instanceof Task _action && _action.shortcut != null) {
            this.tooltip = _action.shortcut;
        }
        if (configurationNode instanceof ObservedFile _observedFile && _observedFile.shortcut != null) {
            this.tooltip = _observedFile.shortcut;
        }
    protected void registerShortcut(String label, String shortcut, Runnable commandAction) {
        if (shortcut == null) {
            return;
        }
        actionId = "org.micoli.php.action." + label.replaceAll("\\s+", "_");

        DynamicShortcutAction action = new DynamicShortcutAction(
                label, "Dynamic action generated by PhpCompanion", PhpCompanionIcon.Execute, commandAction);
        if (ActionManager.getInstance().getAction(actionId) != null) {
            return;
        }
        ActionManager.getInstance().registerAction(actionId, action);
        KeymapManager.getInstance().getActiveKeymap().addShortcut(actionId, parseKeyboardShortcut(shortcut));
    }

    public void unregisterShortcut() {
        if (actionId == null) {
            return;
        }
        KeymapManager.getInstance().getActiveKeymap().removeAllActionShortcuts(actionId);
        ActionManager.getInstance().unregisterAction(actionId);
    }

    private KeyboardShortcut parseKeyboardShortcut(String shortcutString) {
        String[] parts = shortcutString.split("\\s+");
        int modifiers = 0;
        String key = "";

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].toLowerCase();
            if (i == parts.length - 1) {
                key = part;
            } else {
                if (part.equals("ctrl")) modifiers |= InputEvent.CTRL_DOWN_MASK;
                if (part.equals("alt")) modifiers |= InputEvent.ALT_DOWN_MASK;
                if (part.equals("shift")) modifiers |= InputEvent.SHIFT_DOWN_MASK;
                if (part.equals("meta")) modifiers |= InputEvent.META_DOWN_MASK;
            }
        }

        return new KeyboardShortcut(KeyStroke.getKeyStroke(key.toUpperCase().charAt(0), modifiers), null);
    }
 registerShortcut(observedFile.label, observedFile.shortcut, this::toggle);
 TreeUtils.forEachLeaf(tree, (node, path) -> {
 if (node instanceof DynamicTreeNode dynamicTreeNode) {
    dynamicTreeNode.unregisterShortcut();
 }
 });
*/
