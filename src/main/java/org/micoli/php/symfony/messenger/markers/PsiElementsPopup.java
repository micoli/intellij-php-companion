package org.micoli.php.symfony.messenger.markers;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.MouseEvent;
import java.util.List;
import java.util.stream.Collectors;

public class PsiElementsPopup {

    private record PsiElementWrapper(PsiElement element) {
        public String getPresentableText() {
            String base = element.getContainingFile().getName();
            if (element instanceof PsiNamedElement) {
                return base + ": " + ((PsiNamedElement) element).getName();
            }
            return base + ": " + element.getText();
        }

        public void navigate(Project project) {
            if (element instanceof Navigatable) {
                ((Navigatable) element).navigate(true);
            }
        }
    }

    public static void showElementsPopup(Project project, MouseEvent mouseEvent, List<PsiElement> elements) {
        List<PsiElementWrapper> wrappers = elements.stream()
                .map(PsiElementWrapper::new)
                .collect(Collectors.toList());

        BaseListPopupStep<PsiElementWrapper> listPopupStep = new BaseListPopupStep<PsiElementWrapper>(
                "Navigate to Element", wrappers) {

            @Override
            @Nullable
            public PopupStep onChosen(PsiElementWrapper selectedValue, boolean finalChoice) {
                if (finalChoice) {
                    selectedValue.navigate(project);
                }
                return FINAL_CHOICE;
            }

            @Override
            @NotNull
            public String getTextFor(PsiElementWrapper value) {
                return value.getPresentableText();
            }
        };

        JBPopupFactory.getInstance()
            .createListPopup(listPopupStep)
            .show(new RelativePoint(mouseEvent));
        ;
    }
}