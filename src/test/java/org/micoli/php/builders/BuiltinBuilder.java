package org.micoli.php.builders;

import org.micoli.php.tasks.configuration.runnableTask.Builtin;

public class BuiltinBuilder {
    private final Builtin shell;

    private BuiltinBuilder() {
        this.shell = new Builtin();
    }

    public static BuiltinBuilder create() {
        return new BuiltinBuilder();
    }

    public BuiltinBuilder withId(String id) {
        shell.id = id;
        return this;
    }

    public BuiltinBuilder withLabel(String label) {
        shell.label = label;
        return this;
    }

    public BuiltinBuilder withActionId(String actionId) {
        shell.actionId = actionId;
        return this;
    }

    public BuiltinBuilder withIcon(String icon) {
        shell.icon = icon;
        return this;
    }

    public Builtin build() {
        return shell;
    }
}
