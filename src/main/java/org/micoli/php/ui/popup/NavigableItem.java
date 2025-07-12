package org.micoli.php.ui.popup;

import com.intellij.pom.Navigatable;

import javax.swing.*;

public class NavigableItem implements Navigatable {
    private final String fileDescription;
    private final FileExtract fileExtract;
    private final Navigatable navigable;
    private final Icon icon;

    public NavigableItem(String fileDesc, FileExtract fileExtract, Navigatable navigatable, Icon icon) {
        this.fileDescription = fileDesc;
        this.fileExtract = fileExtract;
        this.navigable = navigatable;
        this.icon = icon;
    }

    @Override
    public void navigate(boolean requestFocus) {
        this.navigable.navigate(requestFocus);
    }

    @Override
    public boolean canNavigate() {
        return this.navigable != null;// && element.canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
        return this.navigable != null;// && element.canNavigateToSource();
    }

    public String getFileDesscription() {
        return fileDescription;
    }

    public FileExtract getFileExtract() {
        return fileExtract;
    }

    public Icon getIcon() {
        return icon;
    }
}
