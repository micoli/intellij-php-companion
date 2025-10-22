package org.micoli.php.scripting

import com.intellij.openapi.project.Project
import org.micoli.php.tasks.runnables.RunnableTask.Companion.runBuiltinAction

interface ScriptCore {
    fun runAction(actionId: String)

    fun runActionInEditor(actionId: String)
}

/** known as `core` in scripting engine */
class Core(private val project: Project) : ScriptCore {
    /**
     * Runs a registered action.
     *
     * @param actionId the ID of the action to run
     */
    override fun runAction(actionId: String) {
        runBuiltinAction(project, actionId, false)
    }

    /**
     * Activates the currently opened editor and runs a registered action.
     *
     * @param actionId the ID of the action to run
     */
    override fun runActionInEditor(actionId: String) {
        runBuiltinAction(project, actionId, true)
    }
}
