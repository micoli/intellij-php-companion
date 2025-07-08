package org.micoli.php.symfony.ParseCliDumper;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class ParseCliDumperAction extends AnAction {

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
        e.getPresentation().setText(isVisible ? "Parse Symfony CliDumper" : "Parse Symfony CliDumper - No Selection");
    }

    private void parseCliDumperOutput(String text, Project project) {
        try {
            String parsed = PhpDumpHelper.parseCliDumperToJson(text);

            Messages.showInfoMessage(project, parsed, "Parse CliDumper - Result");
        } catch (Exception ex) {
            Messages.showErrorDialog(project, "Error: \n" + ex.getMessage(), "Parse CliDumper");
        }
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
}
