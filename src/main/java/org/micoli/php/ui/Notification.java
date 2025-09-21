package org.micoli.php.ui;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import javax.swing.Timer;

public class Notification {
    private final Project project;

    public Notification(Project project) {
        this.project = project;
    }

    public static Notification getInstance(Project project) {
        return project.getService(Notification.class);
    }

    public void message(String message) {
        notify(createMessage(message, NotificationType.INFORMATION));
    }

    public void message(String title, String message) {
        notify(createMessage(title, message, NotificationType.INFORMATION));
    }

    public void messageWithTimeout(String message, int delayInMs) {
        try {
            com.intellij.notification.Notification notification =
                    createMessage(message, NotificationType.INFORMATION).setImportant(false);
            notify(notification);
            ApplicationManager.getApplication().invokeLater(() -> {
                new Timer(delayInMs, e -> {
                            if (!notification.isExpired()) {
                                notification.hideBalloon();
                            }
                        })
                        .start();
            });
        } catch (Exception ignored) {
        }
    }

    public void error(String message) {
        notify(createMessage(message, NotificationType.ERROR));
    }

    public void error(String title, String message) {
        notify(createMessage(title, message, NotificationType.ERROR));
    }

    private void notify(com.intellij.notification.Notification message) {
        message.notify(project);
    }

    private com.intellij.notification.Notification createMessage(String message, NotificationType notificationType) {

        return NotificationGroupManager.getInstance()
                .getNotificationGroup("PHP Companion")
                .createNotification(message, notificationType);
    }

    private com.intellij.notification.Notification createMessage(
            String title, String message, NotificationType notificationType) {

        return NotificationGroupManager.getInstance()
                .getNotificationGroup("PHP Companion")
                .createNotification(title, message, notificationType);
    }
}
