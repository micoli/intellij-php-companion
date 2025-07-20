package org.micoli.php.ui.panels;

import com.intellij.icons.AllIcons;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;

public class ActionIconRenderer implements TableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel label = new JLabel();
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setIcon(AllIcons.Actions.Lightning);

        if (isSelected) {
            label.setBackground(table.getSelectionBackground());
            label.setForeground(table.getSelectionForeground());
        } else {
            label.setBackground(table.getBackground());
            label.setForeground(table.getForeground());
        }

        label.setOpaque(true);
        return label;
    }
}
