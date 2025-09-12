package org.micoli.php.builders;

import org.micoli.php.tasks.configuration.runnableTask.Shell;

public class ShellBuilder {
    private final Shell shell;

    private ShellBuilder() {
        this.shell = new Shell();
    }

    public static ShellBuilder create() {
        return new ShellBuilder();
    }

    public ShellBuilder withId(String id) {
        shell.id = id;
        return this;
    }

    public ShellBuilder withLabel(String label) {
        shell.label = label;
        return this;
    }

    public ShellBuilder withCommand(String command) {
        shell.command = command;
        return this;
    }

    public ShellBuilder withCwd(String cwd) {
        shell.cwd = cwd;
        return this;
    }

    public ShellBuilder withIcon(String icon) {
        shell.icon = icon;
        return this;
    }

    public Shell build() {
        return shell;
    }
}
