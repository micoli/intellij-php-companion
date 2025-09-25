package org.micoli.php.tasks

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.util.containers.stream
import io.ktor.util.reflect.instanceOf
import java.nio.file.FileSystems
import java.nio.file.PathMatcher
import java.util.function.Function
import kotlin.Boolean
import kotlin.IllegalStateException
import kotlin.collections.HashMap
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.get
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap
import org.micoli.php.events.TaskNodeChangedEvents
import org.micoli.php.service.DebouncedRunnables
import org.micoli.php.service.filesystem.FileListener
import org.micoli.php.service.filesystem.FileListener.VfsHandler
import org.micoli.php.tasks.configuration.TasksConfiguration
import org.micoli.php.tasks.configuration.Watcher
import org.micoli.php.tasks.configuration.runnableTask.Builtin
import org.micoli.php.tasks.configuration.runnableTask.ObservedFile
import org.micoli.php.tasks.configuration.runnableTask.Script
import org.micoli.php.tasks.configuration.runnableTask.Shell
import org.micoli.php.tasks.configuration.runnableTask.postToggle.PostToggleBuiltin
import org.micoli.php.tasks.configuration.runnableTask.postToggle.PostToggleScript
import org.micoli.php.tasks.configuration.runnableTask.postToggle.PostToggleShell
import org.micoli.php.tasks.models.TaskIdentifier
import org.micoli.php.tasks.runnables.FileObserverTask
import org.micoli.php.tasks.runnables.RunnableTask
import org.micoli.php.ui.Notification

open class TasksService(private val project: Project) : VfsHandler<TaskIdentifier> {
    private val fileListener: FileListener<TaskIdentifier> = FileListener(this)
    protected var runnableActions: ImmutableMap<String, RunnableTask> = persistentMapOf()
    protected var debouncedRunnables: DebouncedRunnables = DebouncedRunnables()
    var isWatcherEnabled: Boolean = true
        private set

    init {
        project.messageBus
            .connect()
            .subscribe<BulkFileListener>(
                VirtualFileManager.VFS_CHANGES, this.fileListener.vfsListener)
    }

    fun loadConfiguration(tasksConfiguration: TasksConfiguration?) {
        fileListener.reset()
        debouncedRunnables.reset()
        runnableActions = persistentMapOf()
        if (tasksConfiguration == null || !tasksConfiguration.enabled) {
            return
        }

        tasksConfiguration.assertConfigurationIsValid()

        initializeRunnableActions(tasksConfiguration)
        initializeFileListener(tasksConfiguration)
        registerTaskActions()
        refreshObservedFiles(false)
    }

    private fun initializeFileListener(tasksConfiguration: TasksConfiguration) {
        val pathMatcherMap: MutableMap<TaskIdentifier, MutableList<PathMatcher>> = HashMap()
        pathMatcherMap.putAll(getWatchedFilesFromWatcher(tasksConfiguration))
        pathMatcherMap.putAll(getWatchedFilesFromObservedFiles(tasksConfiguration))
        fileListener.setPatterns(pathMatcherMap)
    }

    open fun runTask(taskId: String?) {
        val runnableAction: RunnableTask = runnableActions[taskId] ?: return
        runnableAction.run()
        if (runnableAction is FileObserverTask) {
            updateFileObserver(runnableAction, true)
        }
    }

    fun refreshObservedFiles(forceUpdateAll: Boolean) {
        for (taskConfiguration in runnableActions) {
            if (taskConfiguration.value is FileObserverTask) {
                updateFileObserver(taskConfiguration.value as FileObserverTask, forceUpdateAll)
            }
        }
    }

    override fun vfsHandle(taskIdentifier: TaskIdentifier, file: VirtualFile) {
        val runnableTask = runnableActions[taskIdentifier.taskId] ?: return
        if (runnableTask is FileObserverTask) {
            updateFileObserver(runnableTask, true)
            return
        }
        if (taskIdentifier.configuration is Watcher) {
            val watcherConfiguration = taskIdentifier.configuration
            if (!this.isWatcherEnabled) {
                return
            }
            this.debouncedRunnables.run(
                runnableTask, taskIdentifier.taskId, watcherConfiguration.debounce.toLong()) {
                    if (!watcherConfiguration.notify) {
                        return@run
                    }
                    Notification.getInstance(project)
                        .messageWithTimeout(
                            String.format("%s done", watcherConfiguration.taskId), 800)
                }
            return
        }
        runnableTask.run()
    }

    protected open fun updateFileObserver(fileObserverTask: FileObserverTask, force: Boolean) {
        if (!(force || fileObserverTask.hasChanged())) {
            return
        }
        project.messageBus
            .syncPublisher<TaskNodeChangedEvents>(TaskNodeChangedEvents.NODE_CHANGED_EVENTS_TOPIC)
            .setNodeChangedEventsTopic(
                fileObserverTask.taskId, fileObserverTask.status, fileObserverTask.iconAndPrefix)
    }

    fun toggleWatcherEnabled() {
        this.isWatcherEnabled = !this.isWatcherEnabled
    }

    @Synchronized
    fun registerTaskActions() {
        val actionManager = ActionManager.getInstance()

        actionManager.getActionIdList("phpcompanion.tasks.").forEach {
            actionManager.unregisterAction(it!!)
        }

        if (runnableActions.isEmpty()) {
            return
        }
        for (entry in runnableActions) {
            val actionId = "phpcompanion.tasks." + entry.key
            if (actionManager.getAction(actionId) != null) {
                continue
            }
            actionManager.registerAction(actionId, entry.value.anAction)
        }
    }

    private fun initializeRunnableActions(tasksConfiguration: TasksConfiguration) {
        runnableActions =
            tasksConfiguration.tasks
                .filter { !it.id.isNullOrEmpty() }
                .associate {
                    it.id!! to
                        when (it) {
                            is ObservedFile -> FileObserverTask(project, it)
                            is Builtin -> RunnableTask(project, it)
                            is Shell -> RunnableTask(project, it)
                            is Script -> RunnableTask(project, it)
                            is PostToggleBuiltin -> RunnableTask(project, it)
                            is PostToggleShell -> RunnableTask(project, it)
                            is PostToggleScript -> RunnableTask(project, it)
                            else -> throw IllegalStateException("Unexpected value: $it")
                        }
                }
                .toImmutableMap()
    }

    private fun getWatchedFilesFromObservedFiles(
        tasksConfiguration: TasksConfiguration
    ): ImmutableMap<TaskIdentifier, MutableList<PathMatcher>> {
        return tasksConfiguration.tasks
            .filter { it.instanceOf(ObservedFile::class) }
            .map { it as ObservedFile }
            .filter { it.id != null }
            .associate {
                TaskIdentifier(it.id!!, it) to
                    mutableListOf(FileSystems.getDefault().getPathMatcher("glob:${it.filePath}"))
            }
            .toImmutableMap()
    }

    private fun getWatchedFilesFromWatcher(
        tasksConfiguration: TasksConfiguration
    ): ImmutableMap<TaskIdentifier, MutableList<PathMatcher>> {

        return tasksConfiguration.watchers
            .filter { it.instanceOf(Watcher::class) }
            .filter { it.taskId != null }
            .associate {
                TaskIdentifier(it.taskId!!, it) to
                    it.watches
                        .stream()
                        .map(
                            Function { syntaxAndPattern: String ->
                                FileSystems.getDefault().getPathMatcher(syntaxAndPattern)
                            })
                        .toList()
            }
            .toImmutableMap()
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): TasksService {
            return project.getService(TasksService::class.java)
        }
    }
}
