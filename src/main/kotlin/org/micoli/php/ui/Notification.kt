package org.micoli.php.ui

import com.intellij.notification.Notification
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import java.awt.event.ActionEvent
import javax.swing.Timer

open class Notification(private val project: Project?) {
    open fun message(message: String) {
        notify(createMessage(message, NotificationType.INFORMATION))
    }

    open fun message(title: String, message: String) {
        notify(createMessage(title, message, NotificationType.INFORMATION))
    }

    fun messageWithTimeout(message: String, delayInMs: Int) {
        try {
            val notification =
                createMessage(message, NotificationType.INFORMATION).setImportant(false)
            notify(notification)
            ApplicationManager.getApplication().invokeLater {
                Timer(delayInMs) { _: ActionEvent? ->
                        if (!notification.isExpired) {
                            notification.hideBalloon()
                        }
                    }
                    .start()
            }
        } catch (_: Exception) {}
    }

    open fun error(message: String) {
        notify(createMessage(message, NotificationType.ERROR))
    }

    open fun error(title: String, message: String) {
        notify(createMessage(title, message, NotificationType.ERROR))
    }

    private fun notify(message: Notification) {
        message.notify(project)
    }

    private fun createMessage(message: String, notificationType: NotificationType): Notification {
        return NotificationGroupManager.getInstance()
            .getNotificationGroup("PHP Companion")
            .createNotification(message, notificationType)
    }

    private fun createMessage(
        title: String,
        message: String,
        notificationType: NotificationType
    ): Notification {
        return NotificationGroupManager.getInstance()
            .getNotificationGroup("PHP Companion")
            .createNotification(title, message, notificationType)
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): org.micoli.php.ui.Notification {
            return project.getService(org.micoli.php.ui.Notification::class.java)
        }
    }
}
