package org.micoli.php.builders;

import java.lang.reflect.Array;
import org.micoli.php.tasks.configuration.AbstractNode;
import org.micoli.php.tasks.configuration.Task;
import org.micoli.php.tasks.configuration.TasksConfiguration;
import org.micoli.php.tasks.configuration.Watcher;
import org.micoli.php.tasks.configuration.runnableTask.RunnableTaskConfiguration;

public class TasksConfigurationBuilder {
    private final TasksConfiguration taskConfiguration;

    private TasksConfigurationBuilder() {
        this.taskConfiguration = new TasksConfiguration();
    }

    public static TasksConfigurationBuilder create() {
        return new TasksConfigurationBuilder();
    }

    public TasksConfigurationBuilder withRunnableTaskConfigurations(RunnableTaskConfiguration[] tasks) {
        taskConfiguration.tasks = tasks;
        return this;
    }

    public TasksConfigurationBuilder withAddedRunnableTaskConfiguration(RunnableTaskConfiguration task) {
        taskConfiguration.tasks = appendToArray(taskConfiguration.tasks, task, RunnableTaskConfiguration.class);
        return this;
    }

    public TasksConfigurationBuilder withAbstractNodes(AbstractNode[] nodes) {
        taskConfiguration.tree = nodes;
        return this;
    }

    public TasksConfigurationBuilder withAddedAbstractNode(AbstractNode node) {
        taskConfiguration.tree = appendToArray(taskConfiguration.tree, node, AbstractNode.class);
        return this;
    }

    public TasksConfigurationBuilder withWatchers(Watcher[] watchers) {
        taskConfiguration.watchers = watchers;
        return this;
    }

    public TasksConfigurationBuilder withAddedWatcher(Watcher watcher) {
        taskConfiguration.watchers = appendToArray(taskConfiguration.watchers, watcher, Watcher.class);
        return this;
    }

    public TasksConfigurationBuilder withToolbarTasks(Task[] tasks) {
        taskConfiguration.toolbar = tasks;
        return this;
    }

    public TasksConfigurationBuilder withAddedTaskInToolbar(Task task) {
        taskConfiguration.toolbar = appendToArray(taskConfiguration.toolbar, task, Task.class);
        return this;
    }

    public TasksConfigurationBuilder withEnabled(boolean enabled) {
        taskConfiguration.enabled = enabled;
        return this;
    }

    public TasksConfiguration build() {
        return taskConfiguration;
    }

    @SuppressWarnings("unchecked")
    private <T> T[] appendToArray(T[] originalArray, T newElement, Class<T> componentType) {
        if (originalArray == null) {
            T[] newArray = (T[]) Array.newInstance(componentType, 1);
            newArray[0] = newElement;
            return newArray;
        }

        T[] newArray = (T[]) Array.newInstance(componentType, originalArray.length + 1);
        System.arraycopy(originalArray, 0, newArray, 0, originalArray.length);
        newArray[originalArray.length] = newElement;
        return newArray;
    }
}
