package org.micoli.php.symfony.cliDumpParser.actions;

import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import org.micoli.php.symfony.cliDumpParser.JsonToPhpArrayConverter;
import org.micoli.php.symfony.cliDumpParser.PhpDumpHelper;
import org.micoli.php.ui.popup.ParsedContentDisplayPopup;

public class ParseCliDumperToPhpAction extends AbstractParseCliDumperAction {

    protected void parseCliDumperOutput(String text, Project project) {
        try {
            String parsed = JsonToPhpArrayConverter.convertJsonToPhp(PhpDumpHelper.parseCliDumperToJson(text), "   ");

            ParsedContentDisplayPopup.showPhpPopup(project, parsed);
        } catch (Exception ex) {
            Messages.showErrorDialog(project, "Error: \n" + ex.getMessage(), "Parse CliDumper");
        }
    }

    @Override
    protected void setPresentationText(@NotNull Presentation presentation, boolean isVisible) {
        presentation.setText(isVisible ? "Parse Symfony CliDumper to PHP" : "Parse Symfony CliDumper - No Selection");
    }
}
