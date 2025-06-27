package org.micoli.php.peerNavigation.navigation;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;
import org.micoli.php.peerNavigation.configuration.PeerNavigation;
import org.micoli.php.peerNavigation.configuration.PeerNavigationConfiguration;
import org.micoli.php.symfony.messenger.service.PHPHelper;

public class PeerNavigationGotoDeclarationHandler implements GotoDeclarationHandler {

    private static PeerNavigationConfiguration peerNavigation = new PeerNavigationConfiguration();

    public static void loadConfiguration(PeerNavigationConfiguration _peerNavigation) {
        peerNavigation = _peerNavigation;
    }

    @Override
    public PsiElement @Nullable [] getGotoDeclarationTargets(@Nullable PsiElement sourceElement, int offset, Editor editor) {

        if (sourceElement == null) {
            return null;
        }
        PhpClass sourceClass = PsiTreeUtil.getParentOfType(sourceElement, PhpClass.class);
        if (sourceClass == null) {
            return null;
        }
        String sourceClassFQN = sourceClass.getFQN();

        for (PeerNavigation peer : peerNavigation.peers) {
            Matcher matcher = Pattern.compile(peer.source).matcher(sourceClassFQN);
            if (!matcher.matches()) {
                continue;
            }
            PhpClass target = PHPHelper.getPhpClassByFQN(sourceClass.getProject(), peer.target.replaceAll("\\$0", matcher.group(1).replaceAll("\\\\", "\\\\\\\\")));
            if (target != null) {
                return new PsiElement[] { target };
            }
        }

        return null;
    }
}
