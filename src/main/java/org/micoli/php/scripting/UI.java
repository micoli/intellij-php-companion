package org.micoli.php.scripting;

import com.intellij.openapi.project.Project;
import org.micoli.php.ui.Notification;

/**
 * known as `ui` in scripting engine
 */
public final class UI {
    private final Project project;

    public UI(Project project) {
        this.project = project;
    }
    /**
     * Displays a closable popup
     *
     * @param message the message to display.
     */
    public void alert(String message) {
        Notification.getInstance(project).message(message);
    }

    /**
     * Displays a closable popup and automatically close it after a given delay
     *
     * @param message the message to display.
     * @param delayInMs the delay in milliseconds before the popup is closed.
     */
    public void alert(String message, int delayInMs) {
        Notification.getInstance(project).messageWithTimeout(message, delayInMs);
    }
}
