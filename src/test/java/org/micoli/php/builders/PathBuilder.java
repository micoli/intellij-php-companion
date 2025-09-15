package org.micoli.php.builders;

import org.micoli.php.tasks.configuration.AbstractNode;
import org.micoli.php.tasks.configuration.Path;

public class PathBuilder {
    private final Path path;

    private PathBuilder() {
        this.path = new Path();
    }

    public static PathBuilder create() {
        return new PathBuilder();
    }

    public PathBuilder withLabel(String label) {
        path.label = label;
        return this;
    }

    public PathBuilder withTasks(AbstractNode[] tasks) {
        path.tasks = tasks;
        return this;
    }

    public Path build() {
        return path;
    }
}
