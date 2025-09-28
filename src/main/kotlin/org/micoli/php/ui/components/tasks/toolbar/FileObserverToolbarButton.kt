package org.micoli.php.ui.components.tasks.toolbar

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.impl.PresentationFactory
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.util.messages.MessageBusConnection
import javax.swing.SwingUtilities
import org.micoli.php.events.TaskNodeChangedEvents
import org.micoli.php.tasks.TasksService
import org.micoli.php.tasks.configuration.runnableTask.ObservedFile
import org.micoli.php.ui.components.tasks.helpers.FileObserver
import org.micoli.php.ui.components.tasks.helpers.FileObserver.IconAndPrefix

class FileObserverToolbarButton(private val project: Project, observedFile: ObservedFile) :
    AnAction(observedFile.label), Disposable {
    private val taskId: String = observedFile.id
    private val label: String? = observedFile.label

    private val messageBusConnection: MessageBusConnection = project.messageBus.connect()

    init {
        this.messageBusConnection.subscribe<TaskNodeChangedEvents>(
            TaskNodeChangedEvents.NODE_CHANGED_EVENTS_TOPIC,
            this.taskNodeChangedEvents,
        )
    }

    private val taskNodeChangedEvents: TaskNodeChangedEvents
        get() =
            object : TaskNodeChangedEvents {
                override fun setNodeChangedEventsTopic(
                    taskIdParameter: String,
                    status: FileObserver.Status,
                    iconAndPrefix: IconAndPrefix
                ) {
                    if (taskIdParameter != taskId) {
                        return
                    }
                    SwingUtilities.invokeLater {
                        LOGGER.warn("Received node changed event for " + taskId + "-" + status.name)
                        val presentation = templatePresentation
                        presentation.setText(iconAndPrefix.getPrefix() + label)
                        presentation.icon = iconAndPrefix.icon
                        PresentationFactory.updatePresentation(this@FileObserverToolbarButton)
                    }
                }
            }

    override fun actionPerformed(anActionEvent: AnActionEvent) {
        TasksService.getInstance(project).runTask(taskId)
    }

    override fun dispose() {
        messageBusConnection.disconnect()
    }

    companion object {
        private val LOGGER =
            Logger.getInstance(FileObserverToolbarButton::class.java.getSimpleName())
    }
}
