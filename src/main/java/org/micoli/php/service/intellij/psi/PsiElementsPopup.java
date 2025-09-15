package org.micoli.php.service.intellij.psi;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.ui.awt.RelativePoint;
import java.awt.event.MouseEvent;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PsiElementsPopup {
    public static void showLinksToElementsPopup(MouseEvent mouseEvent, List<PsiElement> elements) {
        showPopup(mouseEvent, elements);
    }

    private static void showPopup(MouseEvent mouseEvent, List<PsiElement> elements) {
        JBPopupFactory.getInstance()
                .createListPopup(new BaseListPopupStep<>("Navigate to Element", elements) {
                    @Override
                    public @Nullable PopupStep<?> onChosen(PsiElement selectedValue, boolean finalChoice) {
                        if (finalChoice && selectedValue instanceof Navigatable navigatable) {
                            ApplicationManager.getApplication().invokeLater(() -> navigatable.navigate(true));
                        }
                        return FINAL_CHOICE;
                    }

                    @Override
                    public @NotNull String getTextFor(PsiElement element) {
                        return PsiElementUtil.getHumanReadableElementLink(element);
                    }

                    @Override
                    public boolean isSpeedSearchEnabled() {
                        return true;
                    }
                })
                .show(new RelativePoint(mouseEvent));
    }
}
