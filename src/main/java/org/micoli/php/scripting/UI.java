package org.micoli.php.scripting;

import org.micoli.php.ui.Notification;

public final class UI {
    public void alert(String message) {
        Notification.message(message);
    }

    public void alert(String message, int delayInMs) {
        Notification.messageWithTimeout(message, delayInMs);
    }
}
