package org.micoli.php.peerNavigation.navigation;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;

import org.jetbrains.annotations.Nullable;
import org.micoli.php.peerNavigation.service.PeerNavigationService;

public class PeerNavigationGotoDeclarationHandler implements GotoDeclarationHandler {

    @Override
    public PsiElement @Nullable [] getGotoDeclarationTargets(@Nullable PsiElement sourceElement, int offset, Editor editor) {

        if (sourceElement == null) {
            return null;
        }

        PsiElement targetElement = PeerNavigationService.getPeerElement(sourceElement);

        return targetElement == null ? null : new PsiElement[] { targetElement };
    }
}
