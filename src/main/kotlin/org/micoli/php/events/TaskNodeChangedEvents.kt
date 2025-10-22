package org.micoli.php.events

import com.intellij.util.messages.Topic
import org.micoli.php.ui.components.tasks.helpers.FileObserver
import org.micoli.php.ui.components.tasks.helpers.FileObserver.IconAndPrefix

interface TaskNodeChangedEvents {
    fun setNodeChangedEventsTopic(
        taskIdParameter: String,
        status: FileObserver.Status,
        iconAndPrefix: IconAndPrefix
    )

    companion object {
        val NODE_CHANGED_EVENTS_TOPIC: Topic<TaskNodeChangedEvents> =
            Topic.create<TaskNodeChangedEvents>(
                "PHP Companion task Events", TaskNodeChangedEvents::class.java)
    }
}
