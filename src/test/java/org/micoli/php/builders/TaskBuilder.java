package org.micoli.php.builders;

import org.micoli.php.tasks.configuration.Task;

public class TaskBuilder {
    private final Task task;

    private TaskBuilder() {
        this.task = new Task();
    }

    public static TaskBuilder create() {
        return new TaskBuilder();
    }

    public TaskBuilder withTaskId(String taskId) {
        task.taskId = taskId;
        return this;
    }

    public TaskBuilder withLabel(String label) {
        task.label = label;
        return this;
    }

    public Task build() {
        return task;
    }
}
