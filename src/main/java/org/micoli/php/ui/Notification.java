package org.micoli.php.ui;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.ProjectManager;

public class Notification {
    public static void message(String message) {
        displayMessage(message, NotificationType.INFORMATION);
    }

    public static void error(String message) {
        displayMessage(message, NotificationType.ERROR);
    }

    private static void displayMessage(String message, NotificationType information) {
        com.intellij.notification.NotificationGroupManager.getInstance().getNotificationGroup("PHP Companion").createNotification(message, information).notify(ProjectManager.getInstance().getOpenProjects()[0]);
    }
}
