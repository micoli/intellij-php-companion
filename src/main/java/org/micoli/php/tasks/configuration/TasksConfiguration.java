package org.micoli.php.tasks.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.micoli.php.configuration.models.DisactivableConfiguration;
import org.micoli.php.tasks.configuration.runnableTask.RunnableTaskConfiguration;

public final class TasksConfiguration implements DisactivableConfiguration {
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Schema(description = "Enabler for panel of Task and actions", example = "true")
    public boolean enabled = false;

    @Schema(
            description = "Array of runnable task configurations available in the system. Each task must have a unique"
                    + " identifier to be referenced by tree or toolbar")
    public RunnableTaskConfiguration[] tasks = new RunnableTaskConfiguration[] {};

    @Schema(
            description = "Hierarchical tree structure of tasks and folders for organization in the user interface. Can"
                    + " contain Task objects (referencing tasks by ID) and Path objects (folders containing other"
                    + " nodes)")
    public AbstractNode[] tree = new AbstractNode[] {};

    @Schema(
            description = "Array of tasks to display in the toolbar for quick access. Each element must reference an"
                    + " existing task via its taskId")
    public Task[] toolbar = new Task[] {};

    @Schema(
            description =
                    "File watchers configuration that automatically trigger tasks when specified files are modified")
    public Watcher[] watchers = new Watcher[] {};

    @JsonIgnore()
    public Map<String, RunnableTaskConfiguration> getTasksMap() {
        return Stream.of(tasks)
                .filter((RunnableTaskConfiguration task) -> !task.id.isEmpty())
                .collect(Collectors.toMap(task -> task.id, task -> task, (existing, replacement) -> replacement));
    }

    @JsonIgnore()
    public void assertConfigurationIsValid() {
        Map<String, RunnableTaskConfiguration> tasksMap = getTasksMap();
        assertTreeNodesIsValid(tasksMap, tree);
        if (toolbar != null) {
            for (Task node : toolbar) {
                tasksMap.get(node.taskId);
            }
        }
    }

    @JsonIgnore()
    private void assertTreeNodesIsValid(Map<String, RunnableTaskConfiguration> tasksMap, AbstractNode[] nodes) {
        if (nodes == null) {
            return;
        }
        for (AbstractNode node : nodes) {
            switch (node) {
                case Task task -> tasksMap.get(task.taskId);
                case Path path -> assertTreeNodesIsValid(tasksMap, path.tasks);
                default -> throw new IllegalStateException("Unexpected value: " + node.getClass());
            }
        }
    }
}
