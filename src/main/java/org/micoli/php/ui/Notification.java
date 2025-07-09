package org.micoli.php.ui;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.ProjectManager;
import javax.swing.Timer;

public class Notification {
    public static void message(String message) {
        notify(createMessage(message, NotificationType.INFORMATION));
    }

    public static void messageWithTimeout(String message, int delayInMs) {
        try {
            com.intellij.notification.Notification notification = createMessage(message, NotificationType.INFORMATION).setImportant(false);
            notify(notification);
            ApplicationManager.getApplication().invokeLater(() -> {
                new Timer(delayInMs, e -> {
                    if (!notification.isExpired()) {
                        notification.hideBalloon();
                    }
                }).start();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void error(String message) {
        notify(createMessage(message, NotificationType.ERROR));
    }

    private static void notify(com.intellij.notification.Notification message) {
        message.notify(ProjectManager.getInstance().getOpenProjects()[0]);
    }

    private static com.intellij.notification.Notification createMessage(String message, NotificationType information) {
        return NotificationGroupManager.getInstance().getNotificationGroup("PHP Companion").createNotification(message, information);
    }
}
