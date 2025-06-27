package org.micoli.php.peerNavigation.service;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.micoli.php.peerNavigation.configuration.PeerNavigationConfiguration;
import org.micoli.php.symfony.messenger.service.PHPHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PeerNavigationService {
    private record PeerSourceTarget(Pattern source, String target) {
    }

    private static Project project;
    private static List<PeerSourceTarget> peers = new ArrayList<>();

    public static void loadConfiguration(Project project, PeerNavigationConfiguration _peerNavigation) {
        PeerNavigationService.project = project;
        peers = Arrays.stream(_peerNavigation.peers).map(peer -> new PeerSourceTarget(Pattern.compile(peer.source), peer.target)).toList();
        DaemonCodeAnalyzer.getInstance(project).restart();
    }

    public static @Nullable PsiElement getPeerElement(@NotNull PsiElement sourceElement) {
        PhpClass sourceClass = sourceElement instanceof PhpClass ? (PhpClass) sourceElement : PsiTreeUtil.getParentOfType(sourceElement, PhpClass.class);
        if (sourceClass == null) {
            return null;
        }
        String sourceClassFQN = sourceClass.getFQN();

        for (PeerSourceTarget peer : peers) {
            Matcher matcher = peer.source.matcher(sourceClassFQN);
            if (!matcher.find()) {
                continue;
            }
            PhpClass target = PHPHelper.getPhpClassByFQN(project, matcher.replaceFirst(peer.target));
            if (target != null) {
                return target;
            }
        }

        return null;
    }
}
