package org.micoli.php.codeStyle;

import com.intellij.openapi.project.Project;
import com.intellij.psi.codeStyle.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.micoli.php.codeStyle.configuration.CodeStyle;
import org.micoli.php.codeStyle.configuration.CodeStylesSynchronizationConfiguration;
import org.micoli.php.ui.Notification;

public class CodeStylesService {

    private @NotNull final Project project;

    public CodeStylesService(@NotNull Project project) {
        this.project = project;
    }

    public static CodeStylesService getInstance(Project project) {
        return project.getService(CodeStylesService.class);
    }

    public void loadConfiguration(CodeStylesSynchronizationConfiguration codeStyleSynchronizationConfiguration) {
        if (codeStyleSynchronizationConfiguration == null) {
            return;
        }
        if (!codeStyleSynchronizationConfiguration.enabled) {
            return;
        }
        CodeStyleSchemes codeStyleSchemes = CodeStyleSchemes.getInstance();
        CodeStyleSettings settings = codeStyleSchemes.getCurrentScheme().getCodeStyleSettings();
        CommonCodeStyleSettings phpSettings = settings.getCommonSettings("PHP");
        List<String> changes = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        for (CodeStyle codeStyle : codeStyleSynchronizationConfiguration.styles) {
            setCodeStyle(phpSettings, codeStyle, changes, errors);
        }

        Notification instance = Notification.getInstance(project);
        if (!changes.isEmpty()) {
            CodeStyleSettingsManager.getInstance(project).notifyCodeStyleSettingsChanged();
            instance.message("CodeStyle", getListMessage(changes));
        }
        if (!errors.isEmpty()) {
            instance.error("CodeStyle", getListMessage(errors));
        }
    }

    private static @NotNull String getListMessage(List<String> changes) {
        return "<html><ul><li>" + String.join("</li><li>", changes) + "</li></ul></html>";
    }

    private static void setCodeStyle(
            CommonCodeStyleSettings phpSettings, CodeStyle codeStyle, List<String> changes, List<String> errors) {
        try {
            if (codeStyle.styleAttribute == null || codeStyle.value == null) {
                return;
            }
            Field field = CommonCodeStyleSettings.class.getField(codeStyle.styleAttribute);
            Class<?> fieldType = field.getType();
            if (fieldType == boolean.class || fieldType == Boolean.class) {
                boolean boolValue = Boolean.parseBoolean(codeStyle.value);
                if (field.getBoolean(phpSettings) != boolValue) {
                    changes.add(String.format("%s: %s", codeStyle.styleAttribute, boolValue ? "true" : "false"));
                    field.setBoolean(phpSettings, boolValue);
                }
            } else if (fieldType == int.class || fieldType == Integer.class) {
                int intValue = Integer.parseInt(codeStyle.value);
                if (field.getInt(phpSettings) != intValue) {
                    changes.add(String.format("%s: %s", codeStyle.styleAttribute, intValue));
                    field.setInt(phpSettings, intValue);
                }
            }
        } catch (NoSuchFieldException e) {
            errors.add(String.format("Unknown attribute %s", codeStyle.styleAttribute));
        } catch (IllegalAccessException e) {
            errors.add(String.format("Can not access to %s", codeStyle.styleAttribute));
        } catch (NumberFormatException e) {
            errors.add(String.format("Impossible to convert to number %s", codeStyle.styleAttribute));
        }
    }
}
