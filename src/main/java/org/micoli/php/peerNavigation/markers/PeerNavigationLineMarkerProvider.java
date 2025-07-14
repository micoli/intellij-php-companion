package org.micoli.php.peerNavigation.markers;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.*;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.micoli.php.peerNavigation.service.PeerNavigationService;
import org.micoli.php.service.PsiElementUtil;
import org.micoli.php.service.PsiElementsPopup;
import org.micoli.php.ui.Notification;

public class PeerNavigationLineMarkerProvider implements LineMarkerProvider {

    Icon navigateIcon = IconLoader.getIcon("icons/link.svg", PeerNavigationLineMarkerProvider.class);

    public PeerNavigationLineMarkerProvider() {}

    @Override
    public @Nullable LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(
            @NotNull List<? extends PsiElement> elements, @NotNull Collection<? super LineMarkerInfo<?>> result) {
        if (PeerNavigationService.configurationIsEmpty()) {
            return;
        }
        for (PsiElement element : elements) {
            if (element instanceof PhpClass phpClass) {
                processPhpClass(phpClass, result);
            }
        }
    }

    private void processPhpClass(PhpClass phpClass, Collection<? super LineMarkerInfo<?>> result) {
        List<PsiElement> targetElements = PeerNavigationService.getPeersElement(phpClass);

        if (targetElements == null) {
            return;
        }

        PsiElement leafElement = PsiElementUtil.findFirstLeafElement(phpClass);

        String tooltip = "Search for peer of [" + phpClass.getFQN() + "]";
        result.add(new LineMarkerInfo<>(
                leafElement,
                leafElement.getTextRange(),
                navigateIcon,
                psiElement -> tooltip,
                (mouseEvent, elt) -> navigateToAssociatedElements(mouseEvent, targetElements),
                GutterIconRenderer.Alignment.CENTER,
                () -> tooltip));
    }

    private static void navigateToAssociatedElements(MouseEvent mouseEvent, List<PsiElement> targetElements) {
        if (targetElements.isEmpty()) {
            Notification.error("No peer found");
            return;
        }
        if (targetElements.size() == 1) {
            if (targetElements.getFirst() instanceof Navigatable) {
                ((Navigatable) targetElements.getFirst()).navigate(true);
            }
            return;
        }
        PsiElementsPopup.showLinksToElementsPopup(mouseEvent, targetElements);
    }
}
