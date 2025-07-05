package org.micoli.php.peerNavigation.service;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
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
        if (_peerNavigation == null) {
            return;
        }

        PeerNavigationService.project = project;

        // format:off
        peers.addAll(Arrays.stream(_peerNavigation.peers)
            .map(peer -> new PeerSourceTarget(Pattern.compile(peer.source), peer.target))
            .toList());
        String patternNamedGroup = "\\(\\?<(?<namedGroup>.*?)>.*?\\)";
        String namedGroupReplacement = "\\${${namedGroup}}";
        peers.addAll(Arrays.stream(_peerNavigation.associates)
            .map(associate -> new PeerSourceTarget(
                Pattern.compile(associate.classA),
                associate.classB.replaceAll(patternNamedGroup, namedGroupReplacement)
            ))
            .toList());
        peers.addAll(Arrays.stream(_peerNavigation.associates)
            .map(associate -> new PeerSourceTarget(
                Pattern.compile(associate.classB),
                associate.classA.replaceAll(patternNamedGroup, namedGroupReplacement)
            ))
            .toList());
        // format:on
    }

    public static @Nullable List<PsiElement> getPeersElement(@NotNull PsiElement sourceElement) {
        if (!(sourceElement instanceof PhpClass sourceClass)) {
            return null;
        }
        String sourceClassFQN = sourceClass.getFQN();
        ArrayList<PsiElement> result = new ArrayList<>();

        for (PeerSourceTarget peer : peers) {
            Matcher matcher = peer.source.matcher(sourceClassFQN);
            if (!matcher.find()) {
                continue;
            }
            PhpClass target = PHPHelper.getPhpClassByFQN(project, matcher.replaceFirst(peer.target));
            if (target != null) {
                result.add(target);
            }
        }

        return result.isEmpty() ? null : result;
    }

    public static Boolean configurationIsEmpty() {
        return peers.isEmpty();
    }
}
