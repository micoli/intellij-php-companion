package org.micoli.php.configuration.schema.valueGenerator;

import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

public class CodeStyleGenerator implements PropertyValueGenerator {
    List<String> fieldNames = List.of("styleAttribute");

    @Override
    public List<String> getFieldNames() {
        return fieldNames;
    }

    @Override
    public List<String> getValues() {
        Field[] fields = CommonCodeStyleSettings.class.getFields();
        return Arrays.stream(fields)
                .filter(field -> Modifier.isPublic(field.getModifiers()))
                .filter(field -> {
                    Class<?> fieldType = field.getType();
                    return fieldType == int.class
                            || fieldType == Integer.class
                            || fieldType == boolean.class
                            || fieldType == Boolean.class;
                })
                .map(Field::getName)
                .distinct()
                .sorted()
                .toList();
    }
}
