package org.micoli.php.events;

import com.intellij.util.messages.Topic;
import org.micoli.php.ui.components.tasks.helpers.FileObserver;

public interface TaskNodeChangedEvents {
    Topic<TaskNodeChangedEvents> NODE_CHANGED_EVENTS_TOPIC =
            Topic.create("PHP Companion task Events", TaskNodeChangedEvents.class);

    void setNodeChangedEventsTopic(String taskId, FileObserver.Status status, FileObserver.IconAndPrefix iconAndPrefix);
}
