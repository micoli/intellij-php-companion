package org.micoli.php.configuration.schema.valueGenerator;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.DummyIcon;
import com.intellij.ui.icons.CachedImageIcon;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.*;

public class IconValueGenerator implements PropertyValueGenerator {
    List<String> fieldNames = List.of("icon", "activeIcon", "inactiveIcon", "unknownIcon");

    @Override
    public ReferenceType getType() {
        return ReferenceType.AS_REF;
    }

    @Override
    public String getRefId() {
        return "icons";
    }

    @Override
    public List<String> getFieldNames() {
        return fieldNames;
    }

    @Override
    public List<String> getValues() {
        List<String> iconNames = new ArrayList<>();

        iconNames.addAll(getIconsFromClass(AllIcons.Actions.class));
        iconNames.addAll(getIconsFromClass(AllIcons.Breakpoints.class));
        iconNames.addAll(getIconsFromClass(AllIcons.Chooser.class));
        iconNames.addAll(getIconsFromClass(AllIcons.CodeWithMe.class));
        iconNames.addAll(getIconsFromClass(AllIcons.CodeWithMe.class));
        iconNames.addAll(getIconsFromClass(AllIcons.Debugger.class));
        iconNames.addAll(getIconsFromClass(AllIcons.FileTypes.class));
        iconNames.addAll(getIconsFromClass(AllIcons.FileTypes.class));
        iconNames.addAll(getIconsFromClass(AllIcons.General.class));
        iconNames.addAll(getIconsFromClass(AllIcons.Graph.class));
        iconNames.addAll(getIconsFromClass(AllIcons.Gutter.class));
        iconNames.addAll(getIconsFromClass(AllIcons.Ide.class));
        iconNames.addAll(getIconsFromClass(AllIcons.Json.class));
        iconNames.addAll(getIconsFromClass(AllIcons.Language.class));
        iconNames.addAll(getIconsFromClass(AllIcons.Linux.class));
        iconNames.addAll(getIconsFromClass(AllIcons.Mac.class));
        iconNames.addAll(getIconsFromClass(AllIcons.Nodes.class));
        iconNames.addAll(getIconsFromClass(AllIcons.Plugins.class));
        iconNames.addAll(getIconsFromClass(AllIcons.Profiler.class));
        iconNames.addAll(getIconsFromClass(AllIcons.Run.class));
        iconNames.addAll(getIconsFromClass(AllIcons.Scope.class));
        iconNames.addAll(getIconsFromClass(AllIcons.Status.class));

        return iconNames.stream().filter(Objects::nonNull).distinct().sorted().toList();
    }

    private List<String> getIconsFromClass(Class<?> clazz) {
        List<String> result = new ArrayList<>();
        try {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (!Icon.class.isAssignableFrom(field.getType())) {
                    continue;
                }
                field.setAccessible(true);
                result.add(getIconPath(field));
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private String getIconPath(Field field) {
        try {
            Icon icon = (Icon) field.get(null);
            if (icon instanceof CachedImageIcon cachedImageIcon) {
                return cachedImageIcon.getExpUIPath();
            }
            if (icon instanceof DummyIcon dummyIcon) {
                return dummyIcon.getExpUIPath();
            }
            System.out.println("Unknown icon type: " + icon.getClass().getName());
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
