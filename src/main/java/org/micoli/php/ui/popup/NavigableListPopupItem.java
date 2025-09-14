package org.micoli.php.ui.popup;

import com.intellij.pom.Navigatable;
import org.jetbrains.annotations.NotNull;

public interface NavigableListPopupItem extends Navigatable {
    @NotNull String getText();
}
