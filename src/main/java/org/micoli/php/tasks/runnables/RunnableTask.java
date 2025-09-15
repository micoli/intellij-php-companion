package org.micoli.php.tasks.runnables;

import com.intellij.ide.script.IdeScriptEngine;
import com.intellij.ide.script.IdeScriptEngineManager;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.terminal.ui.TerminalWidget;
import javax.swing.*;
import org.jetbrains.plugins.terminal.TerminalToolWindowFactory;
import org.jetbrains.plugins.terminal.TerminalToolWindowManager;
import org.micoli.php.scripting.FileSystem;
import org.micoli.php.scripting.UI;
import org.micoli.php.tasks.configuration.runnableTask.*;
import org.micoli.php.tasks.configuration.runnableTask.postToggle.PostToggleBuiltin;
import org.micoli.php.tasks.configuration.runnableTask.postToggle.PostToggleScript;
import org.micoli.php.tasks.configuration.runnableTask.postToggle.PostToggleShell;
import org.micoli.php.ui.Notification;

public class RunnableTask implements Runnable {

    private static final Logger LOGGER = Logger.getInstance(RunnableTask.class.getSimpleName());
    private final RunnableTaskConfiguration configuration;
    private final Project project;

    public RunnableTask(Project project, RunnableTaskConfiguration configuration) {
        this.configuration = configuration;
        this.project = project;
    }

    @Override
    public void run() {
        switch (configuration) {
            case Builtin builtin -> runBuiltinAction(builtin.actionId);
            case Shell action -> runShellAction(action.label, action.command, action.cwd);
            case Script script -> runScript(script.extension, script.source);
            case PostToggleBuiltin builtin -> runBuiltinAction(builtin.actionId);
            case PostToggleShell action -> runShellAction(action.label, action.command, action.cwd);
            case PostToggleScript script -> runScript(script.extension, script.source);
            default -> throw new IllegalStateException("Unexpected value: " + configuration);
        }
    }

    private void runScript(String extension, String source) {
        IdeScriptEngine engine = IdeScriptEngineManager.getInstance().getEngineByFileExtension(extension, null);
        if (engine == null) {
            Notification.error(String.format("Script engine with extension '%s' is not found", extension));
            return;
        }
        try {
            engine.setBinding("ui", new UI());
            engine.setBinding("fs", new FileSystem(project));
            engine.eval(source);
        } catch (Exception e) {
            Notification.error(e.getMessage());
        }
    }

    private void runShellAction(String label, String command, String cwd) {
        String workingDirectory = cwd != null ? cwd : project.getBasePath();
        TerminalWidget terminalWidget =
                TerminalToolWindowManager.getInstance(project).createShellWidget(workingDirectory, label, true, true);

        ToolWindow window =
                ToolWindowManager.getInstance(project).getToolWindow(TerminalToolWindowFactory.TOOL_WINDOW_ID);
        if (window == null) {
            return;
        }
        if (!window.isActive()) {
            window.activate(null);
        }

        ApplicationManager.getApplication().invokeLater(() -> {
            terminalWidget.sendCommandToExecute(command);
        });
    }

    private void runBuiltinAction(String actionId) {
        ActionManager actionManager = ActionManager.getInstance();
        AnAction action = actionManager.getAction(actionId);

        if (action == null) {
            Notification.error(String.format("Action '%s' doesn't exist.", actionId));
            return;
        }

        try {
            Editor activeEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
            if (activeEditor == null) {
                return;
            }
            JComponent component = activeEditor.getComponent();
            ActionManager.getInstance().tryToExecute(action, null, component, ActionPlaces.UNKNOWN, true);
        } catch (Exception e) {
            Notification.error(String.format("Error while executing '%s' : %s", actionId, e.getMessage()));
        }
    }
}
