package org.micoli.php.scripting;

import org.micoli.php.ui.Notification;

/**
 * known as `ui` in scripting engine
 */
public final class UI {
    /**
     * Displays a closable popup
     *
     * @param message the message to display.
     */
    public void alert(String message) {
        Notification.message(message);
    }

    /**
     * Displays a closable popup and automatically close it after a given delay
     *
     * @param message the message to display.
     * @param delayInMs the delay in milliseconds before the popup is closed.
     */
    public void alert(String message, int delayInMs) {
        Notification.messageWithTimeout(message, delayInMs);
    }
}
