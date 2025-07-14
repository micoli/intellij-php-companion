package org.micoli.php.ui.popup;

import java.awt.*;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;

public class NavigableListCellRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(
            JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

        if (value instanceof NavigableItem item) {
            return getNavigableItemLabel(list, index, isSelected, cellHasFocus, item);
        }

        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }

    private @NotNull JLabel getNavigableItemLabel(
            JList<?> list, int index, boolean isSelected, boolean cellHasFocus, NavigableItem item) {
        JLabel label = (JLabel) super.getListCellRendererComponent(
                list, item.getFileExtract().lineNumber(), index, isSelected, cellHasFocus);

        label.setIcon(item.getIcon());

        label.setText(String.format(
                "<html><div style=\"padding:5px\"><i style='color: gray;'>%s:%d</i><br/><code style=\"margin-left:5px\">%s</code></div></html>",
                item.getFileDesscription(),
                item.getFileExtract().lineNumber(),
                item.getFileExtract().text().replaceAll("\n", "<br/>")));

        return label;
    }
}
