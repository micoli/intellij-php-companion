package org.micoli.php.builders;

import java.lang.reflect.Array;
import org.micoli.php.codeStyle.configuration.CodeStyle;
import org.micoli.php.codeStyle.configuration.CodeStylesSynchronizationConfiguration;

public class CodeStylesSynchronizationConfigurationBuilder {
    private final CodeStylesSynchronizationConfiguration codeStylesSynchronizationConfiguration;

    private CodeStylesSynchronizationConfigurationBuilder() {
        this.codeStylesSynchronizationConfiguration = new CodeStylesSynchronizationConfiguration();
    }

    public static CodeStylesSynchronizationConfigurationBuilder create() {
        return new CodeStylesSynchronizationConfigurationBuilder();
    }

    public CodeStylesSynchronizationConfigurationBuilder withCodeStyles(CodeStyle[] styles) {
        codeStylesSynchronizationConfiguration.styles = styles;
        return this;
    }

    public CodeStylesSynchronizationConfigurationBuilder withAddedCodeStyle(CodeStyle codeStyle) {
        codeStylesSynchronizationConfiguration.styles =
                appendToArray(codeStylesSynchronizationConfiguration.styles, codeStyle, CodeStyle.class);
        return this;
    }

    public CodeStylesSynchronizationConfigurationBuilder withEnabled(boolean enabled) {
        codeStylesSynchronizationConfiguration.enabled = enabled;
        return this;
    }

    public CodeStylesSynchronizationConfiguration build() {
        return codeStylesSynchronizationConfiguration;
    }

    @SuppressWarnings("unchecked")
    private <T> T[] appendToArray(T[] originalArray, T newElement, Class<T> componentType) {
        if (originalArray == null) {
            T[] newArray = (T[]) Array.newInstance(componentType, 1);
            newArray[0] = newElement;
            return newArray;
        }

        T[] newArray = (T[]) Array.newInstance(componentType, originalArray.length + 1);
        System.arraycopy(originalArray, 0, newArray, 0, originalArray.length);
        newArray[originalArray.length] = newElement;
        return newArray;
    }
}
