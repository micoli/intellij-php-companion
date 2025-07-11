package org.micoli.php.symfony.ParseCliDumper;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractParseCliDumperAction extends AnAction {

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
        boolean isVisible = false;
        String selection = getCurrentSelection(e);
        if (selection != null) {
            isVisible = true;
        }
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
