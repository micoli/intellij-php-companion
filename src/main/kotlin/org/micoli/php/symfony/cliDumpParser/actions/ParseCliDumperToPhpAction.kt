package org.micoli.php.symfony.cliDumpParser.actions

import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import org.micoli.php.symfony.cliDumpParser.JsonToPhpArrayConverter
import org.micoli.php.symfony.cliDumpParser.PhpDumpHelper
import org.micoli.php.ui.popup.ParsedContentDisplayPopup.Companion.showPhpPopup

class ParseCliDumperToPhpAction : AbstractParseCliDumperAction() {
    override fun parseCliDumperOutput(text: String, project: Project) {
        try {
            val jsonString = PhpDumpHelper.parseCliDumperToJson(text) ?: return
            val parsed = JsonToPhpArrayConverter.convertJsonToPhp(jsonString, "   ")

            showPhpPopup(project, parsed)
        } catch (ex: Exception) {
            Messages.showErrorDialog(project, "Error: \n" + ex.message, "Parse CliDumper")
        }
    }

    override fun setPresentationText(presentation: Presentation, isVisible: Boolean) {
        presentation.setText(
            if (isVisible) "Parse Symfony CliDumper to PHP"
            else "Parse Symfony CliDumper - No Selection")
    }
}
