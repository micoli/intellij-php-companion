package org.micoli.php.builders;

import org.micoli.php.tasks.configuration.runnableTask.Script;

public class ScriptBuilder {
    private final Script script;

    private ScriptBuilder() {
        this.script = new Script();
    }

    public static ScriptBuilder create() {
        return new ScriptBuilder();
    }

    public ScriptBuilder withId(String id) {
        script.id = id;
        return this;
    }

    public ScriptBuilder withLabel(String label) {
        script.label = label;
        return this;
    }

    public ScriptBuilder withSource(String source) {
        script.source = source;
        return this;
    }

    public ScriptBuilder withExtension(String extension) {
        script.extension = extension;
        return this;
    }

    public ScriptBuilder withIcon(String icon) {
        script.icon = icon;
        return this;
    }

    public Script build() {
        return script;
    }
}
