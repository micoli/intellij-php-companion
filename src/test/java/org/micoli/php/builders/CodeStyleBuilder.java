package org.micoli.php.builders;

import org.micoli.php.codeStyle.configuration.CodeStyle;

public class CodeStyleBuilder {
    private final CodeStyle codeStyle;

    private CodeStyleBuilder() {
        this.codeStyle = new CodeStyle();
    }

    public static CodeStyleBuilder create() {
        return new CodeStyleBuilder();
    }

    public CodeStyleBuilder withStyleAttribute(String attribute) {
        codeStyle.styleAttribute = attribute;
        return this;
    }

    public CodeStyleBuilder withValue(String value) {
        codeStyle.value = value;
        return this;
    }

    public CodeStyle build() {
        return codeStyle;
    }
}
