package org.micoli.php.peerNavigation.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader.getIcon
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.PhpClass
import java.awt.event.MouseEvent
import javax.swing.Icon
import org.micoli.php.peerNavigation.service.PeerNavigationService
import org.micoli.php.service.intellij.psi.PsiElementUtil.findFirstLeafElement
import org.micoli.php.service.intellij.psi.PsiElementsPopup.showLinksToElementsPopup
import org.micoli.php.ui.Notification.Companion.getInstance

class PeerNavigationLineMarkerProvider : LineMarkerProvider {
    var navigateIcon: Icon = getIcon("icons/link.svg", PeerNavigationLineMarkerProvider::class.java)

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        return null
    }

    override fun collectSlowLineMarkers(
        elements: MutableList<out PsiElement?>,
        result: MutableCollection<in LineMarkerInfo<*>?>
    ) {
        if (elements.isEmpty()) {
            return
        }
        val project = elements.first()?.project ?: return
        val peerNavigationService = PeerNavigationService.getInstance(project)
        if (peerNavigationService.configurationIsEmpty()) {
            return
        }
        for (element in elements) {
            if (element is PhpClass) {
                processPhpClass(project, peerNavigationService, element, result)
            }
        }
    }

    private fun processPhpClass(
        project: Project,
        peerNavigationService: PeerNavigationService,
        phpClass: PhpClass,
        result: MutableCollection<in LineMarkerInfo<*>?>
    ) {
        val targetElements = peerNavigationService.getPeersElement(phpClass) ?: return

        val leafElement = findFirstLeafElement(phpClass)

        val tooltip = "Search for peer of [" + phpClass.fqn + "]"
        result.add(
            LineMarkerInfo(
                leafElement,
                leafElement.textRange,
                navigateIcon,
                { tooltip },
                { mouseEvent: MouseEvent?, _: PsiElement? ->
                    if (mouseEvent == null) {
                        return@LineMarkerInfo
                    }
                    navigateToAssociatedElements(project, mouseEvent, targetElements)
                },
                GutterIconRenderer.Alignment.CENTER,
                { tooltip }))
    }

    private fun navigateToAssociatedElements(
        project: Project,
        mouseEvent: MouseEvent,
        targetElements: MutableList<PsiElement?>
    ) {
        if (targetElements.isEmpty()) {
            getInstance(project).error("No peer found")
            return
        }
        if (targetElements.size == 1) {
            if (targetElements.first() is Navigatable) {
                (targetElements.first() as Navigatable).navigate(true)
            }
            return
        }
        showLinksToElementsPopup(mouseEvent, targetElements)
    }
}
