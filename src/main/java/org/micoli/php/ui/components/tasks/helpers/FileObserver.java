package org.micoli.php.ui.components.tasks.helpers;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.*;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;
import org.micoli.php.service.filesystem.PathUtil;
import org.micoli.php.tasks.configuration.runnableTask.ObservedFile;
import org.micoli.php.ui.Notification;
import org.micoli.php.ui.PhpCompanionIcon;

public class FileObserver {
    private final Project project;

    public static final class IconAndPrefix {
        public Icon icon;
        public String prefix;

        IconAndPrefix(Icon icon, String prefix) {
            this.icon = icon;
            this.prefix = prefix;
        }
    }

    private static final Logger LOGGER = Logger.getInstance(FileObserver.class.getSimpleName());
    private final VirtualFile projectRoot;
    public final ObservedFile observedFile;
    final String activeRegularExpression;
    final String disabledRegularExpression;

    public enum Status {
        Active,
        Inactive,
        Unknown
    }

    Status status;

    public FileObserver(Project project, ObservedFile observedFile) {
        this.projectRoot = PathUtil.getBaseDir(project);
        this.project = project;
        this.observedFile = observedFile;
        activeRegularExpression = "^" + observedFile.variableName + "=";
        disabledRegularExpression = "^" + observedFile.commentPrefix + "\\s*" + observedFile.variableName + "=";
        status = getStatus();
    }

    public boolean toggle() {
        return switch (getStatus()) {
            case Active -> {
                replaceInFile(false);
                yield true;
            }
            case Inactive -> {
                replaceInFile(true);
                yield true;
            }
            default -> false;
        };
    }

    public IconAndPrefix getIconAndPrefix() {
        return switch (this.getStatus()) {
            case Active -> new IconAndPrefix(IconLoader.getIcon(observedFile.activeIcon, PhpCompanionIcon.class), "");
            case Inactive -> new IconAndPrefix(
                    IconLoader.getIcon(observedFile.inactiveIcon, PhpCompanionIcon.class), "# ");
            case Unknown -> new IconAndPrefix(
                    IconLoader.getIcon(observedFile.unknownIcon, PhpCompanionIcon.class), "? ");
        };
    }

    public Status getStatus() {
        VirtualFile file = projectRoot.findFileByRelativePath(observedFile.filePath);
        if (file == null || !file.exists()) {
            return Status.Unknown;
        }
        Status result = Status.Unknown;

        try {
            String content = VfsUtilCore.loadText(file);
            String[] lines = content.split("\n");

            for (String line : lines) {
                if (line.matches(activeRegularExpression + ".*")) {
                    result = Status.Active;
                }
                if (line.matches(disabledRegularExpression + ".*")) {
                    result = Status.Inactive;
                }
            }
        } catch (IOException e) {
            LOGGER.error(e);
        }
        return result;
    }

    public void replaceInFile(boolean toActive) {
        VirtualFile file = projectRoot.findFileByRelativePath(observedFile.filePath);
        if (file == null || !file.exists()) {
            return;
        }

        try {
            WriteAction.run(() -> VfsUtil.saveText(
                    file,
                    replaceInFileContent(toActive, VfsUtilCore.loadText(file)).toString()));
        } catch (IOException e) {
            LOGGER.error(e);
        }
        Notification.getInstance(project)
                .message(observedFile.variableName + " " + (toActive ? "activated" : "deactivated"));
    }

    private @NotNull StringBuilder replaceInFileContent(boolean toActive, String content) {
        StringBuilder result = new StringBuilder();
        String[] lines = content.split("\n");

        for (String line : lines) {
            if (toActive) {
                result.append(line.replaceFirst(disabledRegularExpression, observedFile.variableName + "="));
            } else {
                result.append(line.replaceFirst(
                        activeRegularExpression, observedFile.commentPrefix + observedFile.variableName + "="));
            }
            result.append(System.lineSeparator());
        }
        return result;
    }
}
