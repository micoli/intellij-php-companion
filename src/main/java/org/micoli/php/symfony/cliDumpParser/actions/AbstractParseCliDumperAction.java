package org.micoli.php.symfony.cliDumpParser.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractParseCliDumperAction extends AnAction {

    @Override
    @NotNull
    public ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        String selection = getCurrentSelection(e);
        if (selection == null) {
            return;
        }
        parseCliDumperOutput(selection, e.getProject());
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        boolean isVisible = getCurrentSelection(e) != null;
        e.getPresentation().setVisible(isVisible);
        e.getPresentation().setEnabled(isVisible);
        setPresentationText(e.getPresentation(), isVisible);
    }

    private String getCurrentSelection(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);

        if (editor == null || project == null) {
            return null;
        }
        String selectedText = editor.getSelectionModel().getSelectedText();
        if (selectedText == null || selectedText.trim().isEmpty()) {
            return null;
        }

        return selectedText;
    }

    abstract protected void parseCliDumperOutput(String text, Project project);

    abstract protected void setPresentationText(@NotNull Presentation presentation, boolean isVisible);
}
