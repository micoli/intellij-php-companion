package org.micoli.php.ui.popup;

import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBList;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.*;

public class NavigatableListPopup {

    public static void showNavigablePopup(MouseEvent mouseEvent, List<NavigableItem> elements) {

        JBList<NavigableItem> list = new JBList<>(elements);
        list.setCellRenderer(new NavigableListCellRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        new PopupChooserBuilder<>(list)
                .setItemChosenCallback(() -> {
                    NavigableItem selected = list.getSelectedValue();
                    if (selected != null && selected.canNavigate()) {
                        selected.navigate(true);
                    }
                })
                .createPopup()
                .show(new RelativePoint(mouseEvent));
    }
}
