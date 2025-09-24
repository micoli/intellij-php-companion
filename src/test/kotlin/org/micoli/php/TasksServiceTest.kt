package org.micoli.php

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.micoli.php.builders.ObservedFileBuilder
import org.micoli.php.builders.ScriptBuilder
import org.micoli.php.builders.ShellBuilder
import org.micoli.php.builders.TaskBuilder
import org.micoli.php.builders.TasksConfigurationBuilder
import org.micoli.php.builders.WatcherBuilder
import org.micoli.php.service.DebouncedRunnable
import org.micoli.php.service.DebouncedRunnables
import org.micoli.php.tasks.TasksService
import org.micoli.php.tasks.configuration.TasksConfiguration
import org.micoli.php.tasks.models.TaskIdentifier
import org.micoli.php.tasks.runnables.FileObserverTask
import org.micoli.php.utils.MyFixtureUtils

class TasksServiceTest : BasePlatformTestCase() {
    private var tasksService: TasksService? = null
    private var runnableLogs: ArrayList<String?>? = null

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        runnableLogs = ArrayList()
        tasksService =
            object : TasksService(project) {
                override fun runTask(taskId: String?) {
                    super.runTask(taskId)
                    runnableLogs!!.add(
                        String.format(
                            "%s:%s", taskId, runnableActions[taskId]!!.javaClass.getSimpleName()))
                }

                override fun updateFileObserver(
                    fileObserverTask: FileObserverTask,
                    force: Boolean
                ) {
                    super.updateFileObserver(fileObserverTask, force)
                    runnableLogs!!.add(
                        String.format(
                            "%s:%s",
                            fileObserverTask.taskId,
                            fileObserverTask.javaClass.getSimpleName()))
                }
            }
        val debouncedRunnablesField =
            TasksService::class.java.getDeclaredField("debouncedRunnables")
        debouncedRunnablesField.setAccessible(true)
        debouncedRunnablesField.set(
            tasksService,
            object : DebouncedRunnables() {
                override fun run(
                    task: Runnable,
                    name: String,
                    delayMillis: Long,
                    callback: Runnable?
                ): DebouncedRunnable? {
                    task.run()
                    runnableLogs!!.add(String.format("%s:%s", name, task.javaClass.getSimpleName()))
                    return null
                }
            },
        )
    }

    fun testWatcherEnabled() {
        assertTrue(tasksService!!.isWatcherEnabled)
        tasksService!!.toggleWatcherEnabled()
        assertFalse(tasksService!!.isWatcherEnabled)
        tasksService!!.toggleWatcherEnabled()
        assertTrue(tasksService!!.isWatcherEnabled)
    }

    fun testLoadNullConfiguration() {
        tasksService!!.loadConfiguration(null)
    }

    fun testLoadEmptyConfiguration() {
        // When
        tasksService!!.loadConfiguration(TasksConfiguration())
    }

    fun testLoadConfigurationWithTasks() {
        // When
        tasksService!!.loadConfiguration(
            TasksConfigurationBuilder.create()
                .withAddedRunnableTaskConfiguration(
                    ObservedFileBuilder.create()
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
                .withAddedRunnableTaskConfiguration(
                    ShellBuilder.create()
                        .withId("shell1")
                        .withCommand("echo 'Hello World'")
                        .withCwd(".")
                        .build())
                .build())
    }

    fun testLoadConfigurationWithWatchers() {
        // When
        tasksService!!.loadConfiguration(
            TasksConfigurationBuilder.create()
                .withAddedWatcher(
                    WatcherBuilder.create()
                        .withTaskId("task1")
                        .withWatches(arrayOf("glob:*.txt"))
                        .build())
                .build())
    }

    fun testRunTask() {
        // Given
        val fileContent = "variable=value"
        val filePath = "config.env"
        myFixture.addFileToProject(filePath, fileContent)

        tasksService!!.loadConfiguration(
            TasksConfigurationBuilder.create()
                .withAddedRunnableTaskConfiguration(
                    ObservedFileBuilder.create()
                        .withId("file1")
                        .withFilePath(filePath)
                        .withVariableName("variable")
                        .withCommentPrefix("#")
                        .withActiveIcon("/icons/active.svg")
                        .withInactiveIcon("/icons/inactive.svg")
                        .withUnknownIcon("/icons/unknown.svg")
                        .build())
                .build())

        // When
        tasksService!!.runTask("file1")

        // Then
        assertTrue(runnableLogs!!.stream().anyMatch { s: String? -> s == "file1:FileObserverTask" })
    }

    fun testVfsHandleWithObservedFile() {
        // Given
        val fileContent = "variable=value"
        val filePath = "config.env"
        myFixture.addFileToProject(filePath, fileContent)

        val config =
            TasksConfigurationBuilder.create()
                .withAddedRunnableTaskConfiguration(
                    ObservedFileBuilder.create()
                        .withId("file1")
                        .withFilePath(filePath)
                        .withVariableName("variable")
                        .withCommentPrefix("#")
                        .withActiveIcon("/icons/active.svg")
                        .withInactiveIcon("/icons/inactive.svg")
                        .withUnknownIcon("/icons/unknown.svg")
                        .build())
                .build()
        tasksService!!.loadConfiguration(config)

        val taskIdentifier = TaskIdentifier("file1", config.tasks[0])

        // When
        tasksService!!.vfsHandle(taskIdentifier, myFixture.findFileInTempDir(filePath))

        // Then
        assertTrue(runnableLogs!!.stream().anyMatch { s: String? -> s == "file1:FileObserverTask" })
    }

    fun testVfsHandleShouldBeTriggeredWithWatcher() {
        // Given
        val filePath = "config.txt"
        myFixture.addFileToProject(filePath, "content")
        myFixture.addFileToProject("cache/test.log", "content")

        val config =
            TasksConfigurationBuilder.create()
                .withAddedAbstractNode(
                    TaskBuilder.create().withTaskId("task1").withLabel("Test Task").build())
                .withAddedRunnableTaskConfiguration(
                    ScriptBuilder.create()
                        .withId("task1")
                        .withSource("fs.clearPath(\"cache\",false)")
                        .build())
                .withAddedWatcher(
                    WatcherBuilder.create()
                        .withTaskId("task1")
                        .withWatches(arrayOf("glob:*.txt"))
                        .withDebounce(100)
                        .withNotify(true)
                        .build())
                .build()
        tasksService!!.loadConfiguration(config)

        val taskIdentifier = TaskIdentifier("task1", config.watchers[0])
        TestCase.assertEquals(
            1, MyFixtureUtils.filesMatchingContains(myFixture, "cache/test.log").size)

        // When
        tasksService!!.vfsHandle(taskIdentifier, myFixture.findFileInTempDir(filePath))

        // Then
        TestCase.assertEquals(
            0, MyFixtureUtils.filesMatchingContains(myFixture, "cache/test.log").size)
        assertTrue(runnableLogs!!.stream().anyMatch { s: String? -> s == "task1:RunnableTask" })
    }

    fun testIfActionAreWellRegistered() {
        val tasksConfiguration =
            TasksConfigurationBuilder.create()
                .withAddedRunnableTaskConfiguration(
                    ShellBuilder.create()
                        .withId("shell1")
                        .withCommand("echo 'Hello World'")
                        .withCwd(".")
                        .withIcon("test.svg")
                        .build())
                .build()
        tasksService!!.loadConfiguration(tasksConfiguration)
        tasksService!!.loadConfiguration(tasksConfiguration)
        val actionManager = ActionManager.getInstance()
        val registeredActions = actionManager.getActionIdList("phpcompanion.tasks.")
        TestCase.assertEquals(1, registeredActions.size)
        TestCase.assertEquals("phpcompanion.tasks.shell1", registeredActions[0])
        val action = actionManager.getAction("phpcompanion.tasks.shell1")
        TestCase.assertEquals("shell1", action.templateText)
    }
}
