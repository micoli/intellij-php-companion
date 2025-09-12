package org.micoli.php.builders;

import org.micoli.php.tasks.configuration.runnableTask.PostToggleScript;

public class PostToggleScriptBuilder {
    private final PostToggleScript script;

    private PostToggleScriptBuilder() {
        this.script = new PostToggleScript();
    }

    public static PostToggleScriptBuilder create() {
        return new PostToggleScriptBuilder();
    }

    public PostToggleScriptBuilder withId(String id) {
        script.id = id;
        return this;
    }

    public PostToggleScriptBuilder withLabel(String label) {
        script.label = label;
        return this;
    }

    public PostToggleScriptBuilder withSource(String source) {
        script.source = source;
        return this;
    }

    public PostToggleScriptBuilder withExtension(String extension) {
        script.extension = extension;
        return this;
    }

    public PostToggleScript build() {
        return script;
    }
}
