package org.micoli.php.builders;

import org.micoli.php.tasks.configuration.runnableTask.postToggle.PostToggleShell;

public class PostToggleActionBuilder {
    private final PostToggleShell action;

    private PostToggleActionBuilder() {
        this.action = new PostToggleShell();
    }

    public static PostToggleActionBuilder create() {
        return new PostToggleActionBuilder();
    }

    public PostToggleActionBuilder withId(String id) {
        action.id = id;
        return this;
    }

    public PostToggleActionBuilder withLabel(String label) {
        action.label = label;
        return this;
    }

    public PostToggleActionBuilder withCommand(String command) {
        action.command = command;
        return this;
    }

    public PostToggleActionBuilder withCwd(String cwd) {
        action.cwd = cwd;
        return this;
    }

    public PostToggleShell build() {
        return action;
    }
}
