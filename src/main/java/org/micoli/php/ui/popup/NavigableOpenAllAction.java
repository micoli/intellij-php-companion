package org.micoli.php.ui.popup;

import com.intellij.pom.Navigatable;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class NavigableOpenAllAction implements NavigableListPopupItem {
    private final List<Navigatable> navigables;

    public NavigableOpenAllAction(List<Navigatable> navigables) {
        this.navigables = navigables;
    }

    @Override
    public void navigate(boolean requestFocus) {
        this.navigables.forEach(navigable -> navigable.navigate(requestFocus));
    }

    @Override
    public boolean canNavigate() {
        return true;
    }

    public boolean canNavigateToSource() {
        return false;
    }

    @NotNull
    public String getText() {
        return String.format(
                "<html><div style=\"padding:5px\"><i style='color: orange;font-weight:bold'>%s</i></div></html>",
                "Open results in Editor");
    }
}
