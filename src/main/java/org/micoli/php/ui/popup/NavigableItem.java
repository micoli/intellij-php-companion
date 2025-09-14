package org.micoli.php.ui.popup;

import com.intellij.pom.Navigatable;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;

public class NavigableItem implements NavigableListPopupItem {
    private final FileExtract fileExtract;
    private final Navigatable navigable;
    private final Icon icon;

    public NavigableItem(@NotNull FileExtract fileExtract, @NotNull Navigatable navigable, Icon icon) {
        this.fileExtract = fileExtract;
        this.navigable = navigable;
        this.icon = icon;
    }

    @Override
    public void navigate(boolean requestFocus) {
        navigable.navigate(requestFocus);
    }

    @Override
    public boolean canNavigate() {
        return true;
    }

    public Navigatable getNavigable() {
        return navigable;
    }

    @Override
    public boolean canNavigateToSource() {
        return true;
    }

    @NotNull public FileExtract getFileExtract() {
        return fileExtract;
    }

    public Icon getIcon() {
        return icon;
    }

    @NotNull public String getText() {
        return String.format(
                """
                <html>
                    <div style="padding:5px">
                        <i style="color: gray;">
                            %s:%d
                        </i>
                        <br/>
                        <code style="margin-left:5px">
                            %s
                        </code>
                    </div>
                </html>
                """,
                fileExtract.file(), fileExtract.lineNumber(), fileExtract.text().replaceAll("\n", "<br/>"));
    }
}
