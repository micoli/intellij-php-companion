package org.micoli.php.tasks.runnables

import com.intellij.ide.script.IdeScriptEngine
import com.intellij.ide.script.IdeScriptEngineManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.terminal.ui.TerminalWidget
import kotlin.Boolean
import kotlin.Exception
import kotlin.IllegalStateException
import org.jetbrains.plugins.terminal.TerminalToolWindowFactory
import org.jetbrains.plugins.terminal.TerminalToolWindowManager
import org.micoli.php.scripting.Core
import org.micoli.php.scripting.FileSystem
import org.micoli.php.scripting.UI
import org.micoli.php.tasks.configuration.runnableTask.Builtin
import org.micoli.php.tasks.configuration.runnableTask.RunnableTaskConfiguration
import org.micoli.php.tasks.configuration.runnableTask.Script
import org.micoli.php.tasks.configuration.runnableTask.Shell
import org.micoli.php.tasks.configuration.runnableTask.TaskWithIcon
import org.micoli.php.tasks.configuration.runnableTask.postToggle.PostToggleBuiltin
import org.micoli.php.tasks.configuration.runnableTask.postToggle.PostToggleScript
import org.micoli.php.tasks.configuration.runnableTask.postToggle.PostToggleShell
import org.micoli.php.ui.Notification
import org.micoli.php.ui.PhpCompanionIcon

open class RunnableTask(
    private val project: Project,
    private val configuration: RunnableTaskConfiguration
) : Runnable {

    override fun run() {
        when (configuration) {
            is Builtin -> runBuiltinAction(project, configuration.actionId!!, true)
            is Shell ->
                runShellAction(configuration.label, configuration.command!!, configuration.cwd)
            is Script -> runScript(configuration.extension, configuration.source!!)
            is PostToggleBuiltin -> runBuiltinAction(project, configuration.actionId!!, true)
            is PostToggleShell ->
                runShellAction(configuration.label, configuration.command!!, configuration.cwd)
            is PostToggleScript -> runScript(configuration.extension, configuration.source!!)
            else -> throw IllegalStateException("Unexpected value: $configuration")
        }
    }

    val anAction: AnAction
        get() {
            val action: AnAction =
                object : AnAction() {
                    override fun actionPerformed(anActionEvent: AnActionEvent) {
                        run()
                    }
                }
            val presentation: Presentation = action.templatePresentation
            presentation.setText(this.configuration.id, false)

            if (this.configuration is TaskWithIcon) {
                val icon = this.configuration.icon
                if (icon != "") {
                    presentation.icon =
                        IconLoader.getIcon(icon, PhpCompanionIcon.Companion::class.java)
                }
            }
            presentation.description =
                "Run task: " +
                    (if (this.configuration.label != null) this.configuration.label
                    else this.configuration.id)
            return action
        }

    private fun runScript(extension: String, source: String) {
        val engine: IdeScriptEngine? =
            IdeScriptEngineManager.getInstance().getEngineByFileExtension(extension, null)
        if (engine == null) {
            Notification.getInstance(project)
                .error(String.format("Script engine with extension '%s' is not found", extension))
            return
        }
        try {
            engine.setBinding("ui", UI(project))
            engine.setBinding("fs", FileSystem(project))
            engine.setBinding("core", Core(project))
            engine.eval(source)
        } catch (e: Exception) {
            Notification.getInstance(project).error(e.localizedMessage)
        }
    }

    private fun runShellAction(label: String?, command: String, cwd: String?) {
        val workingDirectory = cwd ?: project.basePath
        val terminalWidget: TerminalWidget =
            TerminalToolWindowManager.getInstance(project)
                .createShellWidget(workingDirectory, label, true, true)

        val window: ToolWindow =
            ToolWindowManager.getInstance(project)
                .getToolWindow(TerminalToolWindowFactory.TOOL_WINDOW_ID) ?: return
        if (!window.isActive) {
            window.activate(null)
        }

        ApplicationManager.getApplication().invokeLater {
            terminalWidget.sendCommandToExecute(command)
        }
    }

    companion object {
        @JvmStatic
        fun runBuiltinAction(project: Project, actionId: String, activateEditor: Boolean) {
            val actionManager = ActionManager.getInstance()
            val action: AnAction? = actionManager.getAction(actionId)

            if (action == null) {
                Notification.getInstance(project)
                    .error(String.format("Action '%s' doesn't exist.", actionId))
                return
            }

            try {
                if (activateEditor) {
                    val activeEditor: Editor =
                        FileEditorManager.getInstance(project).selectedTextEditor ?: return
                    val component = activeEditor.component
                    ActionManager.getInstance()
                        .tryToExecute(action, null, component, ActionPlaces.UNKNOWN, true)
                } else {
                    ActionManager.getInstance()
                        .tryToExecute(action, null, null, ActionPlaces.UNKNOWN, true)
                }
            } catch (e: Exception) {
                Notification.getInstance(project)
                    .error(String.format("Error while executing '%s' : %s", actionId, e.message))
            }
        }
    }
}
