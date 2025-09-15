package org.micoli.php;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import java.lang.reflect.Field;
import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;
import org.micoli.php.builders.*;
import org.micoli.php.service.DebouncedRunnable;
import org.micoli.php.service.DebouncedRunnables;
import org.micoli.php.tasks.TasksService;
import org.micoli.php.tasks.configuration.TasksConfiguration;
import org.micoli.php.tasks.models.TaskIdentifier;
import org.micoli.php.tasks.runnables.FileObserverTask;
import org.micoli.php.utils.MyFixtureUtils;

public class TasksServiceTest extends BasePlatformTestCase {

    private TasksService tasksService;
    private ArrayList<String> runnableLogs;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        runnableLogs = new ArrayList<>();
        tasksService = new TasksService(getProject()) {
            @Override
            public void runTask(String taskId) {
                super.runTask(taskId);
                runnableLogs.add(String.format(
                        "%s:%s", taskId, runnableActions.get(taskId).getClass().getSimpleName()));
            }

            @Override
            protected void updateFileObserver(FileObserverTask fileObserverTask, boolean force) {
                super.updateFileObserver(fileObserverTask, force);
                runnableLogs.add(String.format(
                        "%s:%s",
                        fileObserverTask.getTaskId(),
                        fileObserverTask.getClass().getSimpleName()));
            }
        };
        Field debouncedRunnablesField = TasksService.class.getDeclaredField("debouncedRunnables");
        debouncedRunnablesField.setAccessible(true);
        debouncedRunnablesField.set(tasksService, new DebouncedRunnables() {
            public DebouncedRunnable run(
                    @NotNull Runnable task, @NotNull String name, long delayMillis, Runnable callback) {
                task.run();
                runnableLogs.add(String.format("%s:%s", name, task.getClass().getSimpleName()));
                return null;
            }
        });
    }

    public void testWatcherEnabled() {
        assertTrue(tasksService.isWatcherEnabled());
        tasksService.toggleWatcherEnabled();
        assertFalse(tasksService.isWatcherEnabled());
        tasksService.toggleWatcherEnabled();
        assertTrue(tasksService.isWatcherEnabled());
    }

    public void testLoadNullConfiguration() {
        tasksService.loadConfiguration(null);
    }

    public void testLoadEmptyConfiguration() {
        // When
        tasksService.loadConfiguration(new TasksConfiguration());
    }

    public void testLoadConfigurationWithTasks() {
        // When
        tasksService.loadConfiguration(TasksConfigurationBuilder.create()
                .withAddedRunnableTaskConfiguration(ObservedFileBuilder.create()
                        .withId("file1")
                        .withFilePath("config.env")
                        .withVariableName("variable")
                        .withCommentPrefix("#")
                        .withActiveIcon("/icons/active.svg")
                        .withInactiveIcon("/icons/inactive.svg")
                        .withUnknownIcon("/icons/unknown.svg")
                        .build())
                .withAddedRunnableTaskConfiguration(
                        ScriptBuilder.create().withId("script1").build())
                .withAddedRunnableTaskConfiguration(ShellBuilder.create()
                        .withId("shell1")
                        .withCommand("echo 'Hello World'")
                        .withCwd(".")
                        .build())
                .build());
    }

    public void testLoadConfigurationWithWatchers() {
        // When
        tasksService.loadConfiguration(TasksConfigurationBuilder.create()
                .withAddedWatcher(WatcherBuilder.create()
                        .withTaskId("task1")
                        .withWatches(new String[] {"glob:*.txt"})
                        .build())
                .build());
    }

    public void testRunTask() {
        // Given
        String fileContent = "variable=value";
        String filePath = "config.env";
        myFixture.addFileToProject(filePath, fileContent);

        tasksService.loadConfiguration(TasksConfigurationBuilder.create()
                .withAddedRunnableTaskConfiguration(ObservedFileBuilder.create()
                        .withId("file1")
                        .withFilePath(filePath)
                        .withVariableName("variable")
                        .withCommentPrefix("#")
                        .withActiveIcon("/icons/active.svg")
                        .withInactiveIcon("/icons/inactive.svg")
                        .withUnknownIcon("/icons/unknown.svg")
                        .build())
                .build());

        // When
        tasksService.runTask("file1");

        // Then
        assertTrue(runnableLogs.stream().anyMatch(s -> s.equals("file1:FileObserverTask")));
    }

    public void testVfsHandleWithObservedFile() {
        // Given
        String fileContent = "variable=value";
        String filePath = "config.env";
        myFixture.addFileToProject(filePath, fileContent);

        TasksConfiguration config = TasksConfigurationBuilder.create()
                .withAddedRunnableTaskConfiguration(ObservedFileBuilder.create()
                        .withId("file1")
                        .withFilePath(filePath)
                        .withVariableName("variable")
                        .withCommentPrefix("#")
                        .withActiveIcon("/icons/active.svg")
                        .withInactiveIcon("/icons/inactive.svg")
                        .withUnknownIcon("/icons/unknown.svg")
                        .build())
                .build();
        tasksService.loadConfiguration(config);

        TaskIdentifier taskIdentifier = new TaskIdentifier("file1", config.tasks[0]);

        // When
        tasksService.vfsHandle(taskIdentifier, myFixture.findFileInTempDir(filePath));

        // Then
        assertTrue(runnableLogs.stream().anyMatch(s -> s.equals("file1:FileObserverTask")));
    }

    public void testVfsHandleShouldBeTriggeredWithWatcher() {
        // Given
        String filePath = "config.txt";
        myFixture.addFileToProject(filePath, "content");
        myFixture.addFileToProject("cache/test.log", "content");

        TasksConfiguration config = TasksConfigurationBuilder.create()
                .withAddedAbstractNode(TaskBuilder.create()
                        .withTaskId("task1")
                        .withLabel("Test Task")
                        .build())
                .withAddedRunnableTaskConfiguration(ScriptBuilder.create()
                        .withId("task1")
                        .withSource("fs.clearPath(\"cache\",false)")
                        .build())
                .withAddedWatcher(WatcherBuilder.create()
                        .withTaskId("task1")
                        .withWatches(new String[] {"glob:*.txt"})
                        .withDebounce(100)
                        .withNotify(true)
                        .build())
                .build();
        tasksService.loadConfiguration(config);

        TaskIdentifier taskIdentifier = new TaskIdentifier("task1", config.watchers[0]);
        assertEquals(
                1,
                MyFixtureUtils.filesMatchingContains(myFixture, "cache/test.log")
                        .size());

        // When
        tasksService.vfsHandle(taskIdentifier, myFixture.findFileInTempDir(filePath));

        // Then
        assertEquals(
                0,
                MyFixtureUtils.filesMatchingContains(myFixture, "cache/test.log")
                        .size());
        assertTrue(runnableLogs.stream().anyMatch(s -> s.equals("task1:RunnableTask")));
    }
}
