package org.micoli.php.symfony.messenger.markers;

import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.micoli.php.service.PsiElementUtil;

import java.awt.event.MouseEvent;
import java.util.List;

public class PsiElementsPopup {
    public static void showLinksToElementsPopup(MouseEvent mouseEvent, List<PsiElement> elements) {
        // format:off
        BaseListPopupStep<PsiElement> listPopupStep = new BaseListPopupStep<PsiElement>("Navigate to Element", elements) {
            @Nullable
            public PopupStep onChosen(PsiElement selectedValue, boolean finalChoice) {
                if (finalChoice) {
                    ((Navigatable) selectedValue).navigate(true);
                }
                return FINAL_CHOICE;
            }

            public @NotNull String getTextFor(PsiElement element) {
                return PsiElementUtil.getHumanReadableElementLink(element);
            }
        };

        JBPopupFactory.getInstance().createListPopup(listPopupStep).show(new RelativePoint(mouseEvent));
        // format:on
    }
}
