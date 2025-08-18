package org.micoli.php.ui.popup;

import java.awt.*;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;

public class NavigableListCellRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(
            JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

        if (value instanceof NavigableListPopupItem item) {
            return getNavigableItemLabel(list, index, isSelected, cellHasFocus, item);
        }

        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }

    private @NotNull JLabel getNavigableItemLabel(
            JList<?> list, int index, boolean isSelected, boolean cellHasFocus, NavigableListPopupItem item) {
        if (item instanceof NavigableOpenSearchAction || item instanceof NavigableOpenAllAction) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, 0, index, isSelected, cellHasFocus);

            label.setText(item.getText());

            return label;
        }

        if (item instanceof NavigableItem navigableItem) {
            FileExtract fileExtract = navigableItem.getFileExtract();
            JLabel label = (JLabel)
                    super.getListCellRendererComponent(list, fileExtract.lineNumber(), index, isSelected, cellHasFocus);

            label.setIcon(navigableItem.getIcon());
            label.setText(navigableItem.getText());

            return label;
        }
        throw new IllegalArgumentException("Unsupported item type");
    }
}
