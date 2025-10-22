package org.micoli.php.scripting

import com.intellij.openapi.project.Project
import org.micoli.php.ui.Notification.Companion.getInstance

interface ScriptUI {
    fun alert(message: String)

    fun alert(message: String, delayInMs: Int)
}

/** known as `ui` in scripting engine */
class UI(private val project: Project) : ScriptUI {
    /**
     * Displays a closable popup
     *
     * @param message the message to display.
     */
    override fun alert(message: String) {
        getInstance(project).message(message)
    }

    /**
     * Displays a closable popup and automatically close it after a given delay
     *
     * @param message the message to display.
     * @param delayInMs the delay in milliseconds before the popup is closed.
     */
    override fun alert(message: String, delayInMs: Int) {
        getInstance(project).messageWithTimeout(message, delayInMs)
    }
}
