package org.micoli.php.ui.components.tasks.tree

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader.getIcon
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.messages.MessageBusConnection
import javax.swing.SwingUtilities
import org.micoli.php.events.TaskNodeChangedEvents
import org.micoli.php.tasks.configuration.runnableTask.ObservedFile
import org.micoli.php.ui.PhpCompanionIcon
import org.micoli.php.ui.components.tasks.helpers.FileObserver
import org.micoli.php.ui.components.tasks.helpers.FileObserver.IconAndPrefix

class FileObserverNode(
    project: Project,
    tree: Tree,
    private val initialLabel: String,
    observedFile: ObservedFile
) :
    DynamicTreeNode(
        project,
        tree,
        observedFile.id!!,
        getIcon(observedFile.unknownIcon, PhpCompanionIcon::class.java),
        initialLabel,
        observedFile,
    ),
    Disposable {
    private val messageBusConnection: MessageBusConnection = project.messageBus.connect()

    init {
        messageBusConnection.subscribe<TaskNodeChangedEvents>(
            TaskNodeChangedEvents.NODE_CHANGED_EVENTS_TOPIC,
            this.taskNodeChangedEvents,
        )
    }

    private val taskNodeChangedEvents: TaskNodeChangedEvents
        get() =
            object : TaskNodeChangedEvents {
                override fun setNodeChangedEventsTopic(
                    taskIdParameter: String?,
                    status: FileObserver.Status?,
                    iconAndPrefix: IconAndPrefix?
                ) {
                    if (taskIdParameter != this@FileObserverNode.taskId) {
                        return
                    }
                    SwingUtilities.invokeLater {
                        setIconAndLabel(
                            iconAndPrefix!!.icon, iconAndPrefix.getPrefix() + initialLabel)
                    }
                }
            }

    override fun dispose() {
        messageBusConnection.disconnect()
    }
}
