package org.micoli.php.ui;

import com.intellij.openapi.util.IconLoader;
import javax.swing.*;

public interface PhpCompanionIcon {
    Icon Regexp = IconLoader.getIcon("expui/fileTypes/regexp.svg", PhpCompanionIcon.class);
    Icon Refresh = IconLoader.getIcon("expui/actions/buildAutoReloadChanges.svg", PhpCompanionIcon.class);
    Icon Execute = IconLoader.getIcon("actions/execute.svg", PhpCompanionIcon.class);
}
