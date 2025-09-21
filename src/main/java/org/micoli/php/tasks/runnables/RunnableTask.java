package org.micoli.php.tasks.runnables;

import com.intellij.ide.script.IdeScriptEngine;
import com.intellij.ide.script.IdeScriptEngineManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.terminal.ui.TerminalWidget;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.terminal.TerminalToolWindowFactory;
import org.jetbrains.plugins.terminal.TerminalToolWindowManager;
import org.micoli.php.scripting.Core;
import org.micoli.php.scripting.FileSystem;
import org.micoli.php.scripting.UI;
import org.micoli.php.tasks.configuration.runnableTask.*;
import org.micoli.php.tasks.configuration.runnableTask.postToggle.PostToggleBuiltin;
import org.micoli.php.tasks.configuration.runnableTask.postToggle.PostToggleScript;
import org.micoli.php.tasks.configuration.runnableTask.postToggle.PostToggleShell;
import org.micoli.php.ui.Notification;
import org.micoli.php.ui.PhpCompanionIcon;

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
            case Builtin builtin -> runBuiltinAction(project, builtin.actionId, true);
            case Shell action -> runShellAction(action.label, action.command, action.cwd);
            case Script script -> runScript(script.extension, script.source);
            case PostToggleBuiltin builtin -> runBuiltinAction(project, builtin.actionId, true);
            case PostToggleShell action -> runShellAction(action.label, action.command, action.cwd);
            case PostToggleScript script -> runScript(script.extension, script.source);
            default -> throw new IllegalStateException("Unexpected value: " + configuration);
        }
    }

    public AnAction getAnAction() {
        AnAction action = new AnAction() {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                run();
            }
        };
        Presentation presentation = action.getTemplatePresentation();
        presentation.setText(this.configuration.id, false);
        if (this.configuration.getIcon() != null
                && !this.configuration.getIcon().isBlank()) {
            presentation.setIcon(IconLoader.getIcon(this.configuration.getIcon(), PhpCompanionIcon.class));
        }
        presentation.setDescription(
                "Run task: " + (this.configuration.label != null ? this.configuration.label : this.configuration.id));
        return action;
    }

    private void runScript(String extension, String source) {
        IdeScriptEngine engine = IdeScriptEngineManager.getInstance().getEngineByFileExtension(extension, null);
        if (engine == null) {
            Notification.getInstance(project)
                    .error(String.format("Script engine with extension '%s' is not found", extension));
            return;
        }
        try {
            engine.setBinding("ui", new UI(project));
            engine.setBinding("fs", new FileSystem(project));
            engine.setBinding("core", new Core(project));
            engine.eval(source);
        } catch (Exception e) {
            Notification.getInstance(project).error(e.getMessage());
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

    public static void runBuiltinAction(Project project, String actionId, boolean activateEditor) {
        ActionManager actionManager = ActionManager.getInstance();
        AnAction action = actionManager.getAction(actionId);

        if (action == null) {
            Notification.getInstance(project).error(String.format("Action '%s' doesn't exist.", actionId));
            return;
        }

        try {
            if (activateEditor) {
                Editor activeEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
                if (activeEditor == null) {
                    return;
                }
                JComponent component = activeEditor.getComponent();
                ActionManager.getInstance().tryToExecute(action, null, component, ActionPlaces.UNKNOWN, true);
            } else {
                ActionManager.getInstance().tryToExecute(action, null, null, ActionPlaces.UNKNOWN, true);
            }
        } catch (Exception e) {
            Notification.getInstance(project)
                    .error(String.format("Error while executing '%s' : %s", actionId, e.getMessage()));
        }
    }
}
