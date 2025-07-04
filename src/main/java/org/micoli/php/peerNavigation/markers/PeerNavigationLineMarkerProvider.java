package org.micoli.php.peerNavigation.markers;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.micoli.php.peerNavigation.service.PeerNavigationService;
import org.micoli.php.service.PsiElementService;

import javax.swing.*;
import java.util.Collection;
import java.util.List;

public class PeerNavigationLineMarkerProvider implements LineMarkerProvider {

    Icon navigateIcon = IconLoader.getIcon("icons/link.svg", PeerNavigationLineMarkerProvider.class);

    public PeerNavigationLineMarkerProvider() {
    }

    @Override
    public @Nullable LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<? extends PsiElement> elements, @NotNull Collection<? super LineMarkerInfo<?>> result) {
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
        PsiElement targetElement = PeerNavigationService.getPeerElement(phpClass);

        if (targetElement == null) {
            return;
        }

        result.add(NavigationGutterIconBuilder.create(navigateIcon).setTargets(targetElement).setTooltipText("Navigate to [" + phpClass.getFQN() + "]").createLineMarkerInfo(PsiElementService.findFirstLeafElement(phpClass)));
    }
}
