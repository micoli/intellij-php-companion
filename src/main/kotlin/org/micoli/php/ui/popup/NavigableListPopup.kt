package org.micoli.php.ui.popup

import com.intellij.openapi.ui.popup.PopupChooserBuilder
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.components.JBList
import java.awt.event.MouseEvent
import javax.swing.ListSelectionModel

object NavigableListPopup {
    @JvmStatic
    fun showNavigablePopup(mouseEvent: MouseEvent, elements: MutableList<NavigableListPopupItem?>) {
        val list = JBList<NavigableListPopupItem?>(elements)
        list.cellRenderer = NavigableListCellRenderer()
        list.selectionMode = ListSelectionModel.SINGLE_SELECTION

        PopupChooserBuilder<NavigableListPopupItem?>(list)
          .setItemChosenCallback(
            Runnable {
                val selected = list.getSelectedValue()
                if (selected != null && selected.canNavigate()) {
                    selected.navigate(true)
                }
            }
          )
          .createPopup()
          .show(RelativePoint(mouseEvent))
    }
}
