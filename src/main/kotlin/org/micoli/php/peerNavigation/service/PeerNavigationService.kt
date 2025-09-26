package org.micoli.php.peerNavigation.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.util.containers.stream
import com.jetbrains.php.lang.psi.elements.PhpClass
import java.util.regex.Pattern
import org.micoli.php.peerNavigation.configuration.Associate
import org.micoli.php.peerNavigation.configuration.Peer
import org.micoli.php.peerNavigation.configuration.PeerNavigationConfiguration
import org.micoli.php.service.intellij.psi.PhpUtil.getPhpClassByFQN

@Service(Service.Level.PROJECT)
class PeerNavigationService(private val project: Project) {
    @JvmRecord private data class PeerSourceTarget(val source: Pattern?, val target: String?)

    private val peers: MutableList<PeerSourceTarget> = ArrayList<PeerSourceTarget>()

    fun loadConfiguration(peerNavigation: PeerNavigationConfiguration?) {
        if (peerNavigation == null) {
            return
        }
        val patternNamedGroup = "\\(\\?<(?<namedGroup>.*?)>.*?\\)"
        val namedGroupReplacement = "\\\${\${namedGroup}}"

        peers.addAll(
            peerNavigation.peers
                .stream()
                .map { peer: Peer -> PeerSourceTarget(Pattern.compile(peer!!.source), peer.target) }
                .toList())
        peers.addAll(
            peerNavigation.associates
                .stream()
                .map { associate: Associate ->
                    PeerSourceTarget(
                        Pattern.compile(associate!!.classA),
                        associate.classB?.replace(
                            patternNamedGroup.toRegex(), namedGroupReplacement))
                }
                .toList())
        peers.addAll(
            peerNavigation.associates
                .stream()
                .map { associate: Associate ->
                    PeerSourceTarget(
                        Pattern.compile(associate!!.classB),
                        associate.classA?.replace(
                            patternNamedGroup.toRegex(), namedGroupReplacement))
                }
                .toList())
    }

    fun getPeersElement(sourceElement: PsiElement): MutableList<PsiElement?>? {
        if (sourceElement !is PhpClass) {
            return null
        }
        val sourceClassFQN = sourceElement.getFQN()
        val result = HashSet<PsiElement?>()

        for (peer in peers) {
            val matcher = peer.source!!.matcher(sourceClassFQN)
            if (!matcher.find()) {
                continue
            }
            val target = getPhpClassByFQN(project, matcher.replaceFirst(peer!!.target))
            if (target != null) {
                result.add(target)
            }
        }

        return if (result.isEmpty()) null else result.stream().toList()
    }

    fun configurationIsEmpty(): Boolean {
        return peers.isEmpty()
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): PeerNavigationService {
            return project.getService(PeerNavigationService::class.java)
        }
    }
}
