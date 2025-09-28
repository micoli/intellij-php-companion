package org.micoli.php.codeStyle

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.codeStyle.CodeStyleSchemes
import com.intellij.psi.codeStyle.CodeStyleSettingsManager
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import kotlin.Boolean
import kotlin.Int
import kotlin.NumberFormatException
import org.micoli.php.codeStyle.configuration.CodeStyle
import org.micoli.php.codeStyle.configuration.CodeStylesSynchronizationConfiguration
import org.micoli.php.ui.Notification

@Service(Service.Level.PROJECT)
class CodeStylesService(private val project: Project) {
    fun loadConfiguration(
        codeStyleSynchronizationConfiguration: CodeStylesSynchronizationConfiguration?
    ) {
        if (codeStyleSynchronizationConfiguration == null) {
            return
        }
        if (!codeStyleSynchronizationConfiguration.enabled) {
            return
        }
        val codeStyleSchemes = CodeStyleSchemes.getInstance()
        val settings = codeStyleSchemes.currentScheme.codeStyleSettings
        val phpSettings = settings.getCommonSettings("PHP")
        val changes: MutableList<String> = ArrayList()
        val errors: MutableList<String> = ArrayList()
        for (codeStyle in codeStyleSynchronizationConfiguration.styles) {
            setCodeStyle(phpSettings, codeStyle, changes, errors)
        }

        val instance = Notification.getInstance(project)
        if (!changes.isEmpty()) {
            CodeStyleSettingsManager.getInstance(project).notifyCodeStyleSettingsChanged()
            instance.message("CodeStyle", getListMessage(changes))
        }
        if (!errors.isEmpty()) {
            instance.error("CodeStyle", getListMessage(errors))
        }
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): CodeStylesService {
            return project.getService(CodeStylesService::class.java)
        }

        private fun getListMessage(changes: MutableList<String>): String {
            return "<html><ul><li>" + changes.joinToString("</li><li>") + "</li></ul></html>"
        }

        private fun setCodeStyle(
            phpSettings: CommonCodeStyleSettings?,
            codeStyle: CodeStyle,
            changes: MutableList<String>,
            errors: MutableList<String>
        ) {
            try {
                if (!codeStyle.isFullyInitialized()) {
                    return
                }
                val field = CommonCodeStyleSettings::class.java.getField(codeStyle.styleAttribute)
                val fieldType = field.type
                if (fieldType == Boolean::class.javaPrimitiveType ||
                    fieldType == Boolean::class.java) {
                    val boolValue = codeStyle.value.toBoolean()
                    if (field.getBoolean(phpSettings) != boolValue) {
                        changes.add(
                            String.format(
                                "%s: %s",
                                codeStyle.styleAttribute,
                                if (boolValue) "true" else "false"))
                        field.setBoolean(phpSettings, boolValue)
                    }
                } else if (fieldType == Int::class.javaPrimitiveType ||
                    fieldType == Int::class.java) {
                    val intValue = codeStyle.value.toInt()
                    if (field.getInt(phpSettings) != intValue) {
                        changes.add("${codeStyle.styleAttribute}: $intValue")
                        field.setInt(phpSettings, intValue)
                    }
                }
            } catch (_: NoSuchFieldException) {
                errors.add(String.format("Unknown attribute %s", codeStyle.styleAttribute))
            } catch (_: IllegalAccessException) {
                errors.add(String.format("Can not access to %s", codeStyle.styleAttribute))
            } catch (_: NumberFormatException) {
                errors.add(
                    String.format("Impossible to convert to number %s", codeStyle.styleAttribute))
            }
        }
    }
}
