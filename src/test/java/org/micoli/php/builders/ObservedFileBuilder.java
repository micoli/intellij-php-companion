package org.micoli.php.builders;

import org.micoli.php.tasks.configuration.runnableTask.ObservedFile;
import org.micoli.php.tasks.configuration.runnableTask.postToggle.PostToggle;

public class ObservedFileBuilder {
    private final ObservedFile observedFile;

    private ObservedFileBuilder() {
        this.observedFile = new ObservedFile();
    }

    public static ObservedFileBuilder create() {
        return new ObservedFileBuilder();
    }

    public ObservedFileBuilder withId(String id) {
        observedFile.id = id;
        return this;
    }

    public ObservedFileBuilder withLabel(String label) {
        observedFile.label = label;
        return this;
    }

    public ObservedFileBuilder withCommentPrefix(String commentPrefix) {
        observedFile.commentPrefix = commentPrefix;
        return this;
    }

    public ObservedFileBuilder withFilePath(String filePath) {
        observedFile.filePath = filePath;
        return this;
    }

    public ObservedFileBuilder withVariableName(String variableName) {
        observedFile.variableName = variableName;
        return this;
    }

    public ObservedFileBuilder withActiveIcon(String activeIcon) {
        observedFile.activeIcon = activeIcon;
        return this;
    }

    public ObservedFileBuilder withInactiveIcon(String inactiveIcon) {
        observedFile.inactiveIcon = inactiveIcon;
        return this;
    }

    public ObservedFileBuilder withUnknownIcon(String unknownIcon) {
        observedFile.unknownIcon = unknownIcon;
        return this;
    }

    public ObservedFileBuilder withPostToggle(PostToggle postToggle) {
        observedFile.postToggle = postToggle;
        return this;
    }

    public ObservedFile build() {
        return observedFile;
    }
}
