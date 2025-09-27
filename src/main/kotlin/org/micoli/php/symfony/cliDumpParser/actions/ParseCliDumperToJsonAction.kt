package org.micoli.php.symfony.cliDumpParser.actions

import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import org.micoli.php.symfony.cliDumpParser.PhpDumpHelper
import org.micoli.php.ui.popup.ParsedContentDisplayPopup.Companion.showJsonPopup

class ParseCliDumperToJsonAction : AbstractParseCliDumperAction() {
    override fun parseCliDumperOutput(text: String, project: Project) {
        try {
            val parsed = PhpDumpHelper.parseCliDumperToJson(text) ?: return

            showJsonPopup(project, parsed)
        } catch (ex: Exception) {
            Messages.showErrorDialog(project, "Error: \n" + ex.message, "Parse CliDumper")
        }
    }

    override fun setPresentationText(presentation: Presentation, isVisible: Boolean) {
        presentation.setText(
            if (isVisible) "Parse Symfony CliDumper to JSON"
            else "Parse Symfony CliDumper - No Selection")
    }
}
