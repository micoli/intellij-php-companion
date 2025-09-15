package org.micoli.php.tasks.runnables;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.micoli.php.tasks.configuration.runnableTask.ObservedFile;
import org.micoli.php.ui.components.tasks.helpers.FileObserver;

public class FileObserverTask extends RunnableTask {
    private static final Logger LOGGER = Logger.getInstance(FileObserverTask.class.getSimpleName());
    private final FileObserver fileObserver;
    private final RunnableTask postToggle;
    private final @NotNull String taskId;

    FileObserver.Status status;
    private boolean firstCheck = true;

    public FileObserverTask(Project project, ObservedFile observedFile) {
        super(project, observedFile);
        fileObserver = new FileObserver(project, observedFile);
        this.taskId = observedFile.id;
        status = fileObserver.getStatus();
        postToggle = observedFile.postToggle == null ? null : new RunnableTask(project, observedFile.postToggle);
    }

    @Override
    public void run() {
        if (!this.fileObserver.toggle()) {
            return;
        }
        if (postToggle != null) {
            postToggle.run();
        }
        hasChanged();
    }

    public boolean hasChanged() {
        FileObserver.Status oldStatus = status;
        status = this.fileObserver.getStatus();
        if (firstCheck || !status.equals(oldStatus)) {
            firstCheck = false;
            return true;
        }
        return false;
    }

    public @NotNull String getTaskId() {
        return taskId;
    }

    public FileObserver.Status getStatus() {
        return status;
    }

    public FileObserver.IconAndPrefix getIconAndPrefix() {
        return fileObserver.getIconAndPrefix();
    }
}
