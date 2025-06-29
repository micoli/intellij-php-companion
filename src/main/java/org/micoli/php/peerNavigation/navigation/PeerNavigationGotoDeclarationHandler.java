package org.micoli.php.peerNavigation.navigation;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;

import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.Nullable;
import org.micoli.php.peerNavigation.service.PeerNavigationService;

import java.util.Objects;

public class PeerNavigationGotoDeclarationHandler implements GotoDeclarationHandler {

    @Override
    public PsiElement @Nullable [] getGotoDeclarationTargets(@Nullable PsiElement sourceElement, int offset, Editor editor) {

        if (sourceElement == null) {
            return null;
        }
        if (!(sourceElement instanceof LeafPsiElement sourceClass)) {
            return null;
        }
        if (!sourceClass.getElementType().toString().equals("identifier")) {
            return null;
        }
        PsiElement targetElement = PeerNavigationService.getPeerElement(sourceClass);

        return targetElement == null ? null : new PsiElement[]{targetElement};
    }
}
