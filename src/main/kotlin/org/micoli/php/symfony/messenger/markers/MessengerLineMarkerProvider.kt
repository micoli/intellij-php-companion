package org.micoli.php.symfony.messenger.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.IconLoader.getIcon
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.MethodReference
import java.awt.event.MouseEvent
import java.util.Objects
import java.util.function.Supplier
import java.util.stream.Collectors
import javax.swing.Icon
import kotlinx.collections.immutable.toImmutableList
import org.micoli.php.service.intellij.psi.PhpUtil.findClassByFQN
import org.micoli.php.service.intellij.psi.PhpUtil.getFirstParameterType
import org.micoli.php.service.intellij.psi.PhpUtil.normalizeNonRootFQN
import org.micoli.php.service.intellij.psi.PsiElementUtil.findFirstLeafElement
import org.micoli.php.service.intellij.psi.PsiElementUtil.getFileExtract
import org.micoli.php.symfony.messenger.service.MessengerService
import org.micoli.php.ui.Notification.Companion.getInstance
import org.micoli.php.ui.popup.NavigableItem
import org.micoli.php.ui.popup.NavigableListPopup.showNavigablePopup
import org.micoli.php.ui.popup.NavigableListPopupItem
import org.micoli.php.ui.popup.NavigableOpenAllAction
import org.micoli.php.ui.popup.NavigableOpenSearchAction

class MessengerLineMarkerProvider : LineMarkerProvider {
    var navigateSendIcon: Icon =
        getIcon("icons/messenger-send-2.svg", MessengerLineMarkerProvider::class.java)
    var navigateReceiveIcon: Icon =
        getIcon("icons/messenger-receive-2.svg", MessengerLineMarkerProvider::class.java)

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
        val messengerService = MessengerService.getInstance(project)
        for (element in elements) {
            if (element is MethodReference) {
                processDispatchMethod(messengerService, element, result)
            }
            if (element is Method) {
                processHandleMethod(messengerService, element, result)
            }
        }
    }

    private fun processDispatchMethod(
        messengerService: MessengerService,
        methodRef: MethodReference?,
        result: MutableCollection<in LineMarkerInfo<*>?>
    ) {
        if (methodRef == null || !messengerService.isDispatchMethod(methodRef.name)) {
            return
        }

        val messageClassName = getFirstParameterType(methodRef.parameters) ?: return

        val handlers = messengerService.findHandlersByMessageName(messageClassName)

        if (handlers.isEmpty()) {
            return
        }

        result.add(
            NavigationGutterIconBuilder.create(navigateSendIcon)
                .setTargets(handlers)
                .setTooltipText("Navigate to message handlers")
                .createLineMarkerInfo(findFirstLeafElement(methodRef)))
    }

    private fun processHandleMethod(
        messengerService: MessengerService,
        method: Method?,
        result: MutableCollection<in LineMarkerInfo<*>?>
    ) {
        if (method == null) {
            return
        }

        if (!messengerService.isHandlerMethod(method.name)) {
            return
        }

        val messageClassName = messengerService.extractMessageClassFromHandler(method) ?: return

        val project = method.project
        val msgClass = findClassByFQN(project, normalizeNonRootFQN(messageClassName)) ?: return
        if (messengerService.isMessageClass(msgClass)) {
            val leafElement = findFirstLeafElement(method)

            result.add(
                LineMarkerInfo(
                    leafElement,
                    leafElement.textRange,
                    navigateReceiveIcon,
                    { "Search for usages of [$messageClassName]" },
                    { mouseEvent: MouseEvent?, _: PsiElement? ->
                        navigateToMessageDispatchCalls(
                            messengerService, mouseEvent!!, project, messageClassName)
                    },
                    GutterIconRenderer.Alignment.CENTER,
                    { "Search for usages of [$messageClassName]" }))
        }
    }

    private fun navigateToMessageDispatchCalls(
        messengerService: MessengerService,
        mouseEvent: MouseEvent,
        project: Project,
        messageClassName: String
    ) {
        messengerService.findDispatchCallsForMessageAsync(messageClassName) { dispatchCalls ->
            val elements = ArrayList<PsiElement?>()
            ApplicationManager.getApplication().runReadAction {
                for (dispatchCall in dispatchCalls!!) {
                    if (dispatchCall?.isValid == false) {
                        continue
                    }
                    elements.add(dispatchCall)
                }
            }
            ApplicationManager.getApplication().invokeLater {
                if (elements.isEmpty()) {
                    getInstance(project).error("No usage found")
                    return@invokeLater
                }
                if (elements.size == 1) {
                    if (elements.first() is Navigatable) {
                        (elements.first() as Navigatable).navigate(true)
                    }
                    return@invokeLater
                }
                val navigableItemList: MutableList<NavigableItem?> =
                    elements
                        .stream()
                        .map { psiElement: PsiElement? ->
                            ApplicationManager.getApplication()
                                .runReadAction<NavigableItem?>(
                                    Computable {
                                        psiElement!!.containingFile ?: return@Computable null
                                        if (!(psiElement as Navigatable).canNavigate()) {
                                            return@Computable null
                                        }

                                        val fileExtract = getFileExtract(psiElement, 0)
                                        NavigableItem(
                                            fileExtract,
                                            psiElement as Navigatable,
                                            psiElement.getIcon(0))
                                    })
                        }
                        .filter { obj: NavigableItem? -> Objects.nonNull(obj) }
                        .collect(Collectors.toCollection(Supplier { ArrayList() }))

                if (!hasMultipleFilesReferenced(navigableItemList)) {
                    showNavigablePopup(
                        mouseEvent,
                        navigableItemList
                            .stream()
                            .map<NavigableListPopupItem?> { item: NavigableItem? ->
                                item as NavigableListPopupItem?
                            }
                            .toList())
                    return@invokeLater
                }
                showNavigablePopup(
                    mouseEvent, addActionsToItems(project, messageClassName, navigableItemList))
            }
        }
    }

    private fun addActionsToItems(
        project: Project,
        messageClassName: String?,
        navigableItemList: MutableList<NavigableItem?>
    ): MutableList<NavigableListPopupItem?> {
        val navigatableList =
            navigableItemList.stream().map { it!!.navigable }.toList().toImmutableList()
        val finalList = ArrayList<NavigableListPopupItem?>(navigableItemList)
        finalList.add(NavigableOpenAllAction(navigatableList))
        finalList.add(
            NavigableOpenSearchAction(
                project,
                navigatableList,
                "Find message dispatch calls $messageClassName",
                messageClassName))
        return finalList
    }

    private fun hasMultipleFilesReferenced(
        navigableItemList: MutableList<NavigableItem?>
    ): Boolean {
        return (navigableItemList
            .stream()
            .map<String?> { navigableItem: NavigableItem? -> navigableItem!!.fileExtract.file }
            .distinct()
            .count() > 1)
    }
}
