package org.micoli.php.symfony.ParseCliDumper.actions;

import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import org.micoli.php.service.ParsedContentDisplayPopup;
import org.micoli.php.symfony.ParseCliDumper.PhpDumpHelper;

public class ParseCliDumperToJsonAction extends AbstractParseCliDumperAction {

    protected void parseCliDumperOutput(String text, Project project) {
        try {
            String parsed = PhpDumpHelper.parseCliDumperToJson(text);

            ParsedContentDisplayPopup.showJsonPopup(project, parsed);
        } catch (Exception ex) {
            Messages.showErrorDialog(project, "Error: \n" + ex.getMessage(), "Parse CliDumper");
        }
    }

    @Override
    protected void setPresentationText(@NotNull Presentation presentation, boolean isVisible) {
        presentation.setText(isVisible ? "Parse Symfony CliDumper to JSON" : "Parse Symfony CliDumper - No Selection");
    }
}
