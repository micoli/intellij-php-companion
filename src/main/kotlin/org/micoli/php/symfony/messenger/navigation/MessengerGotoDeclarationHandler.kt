package org.micoli.php.symfony.messenger.navigation

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.psi.elements.MethodReference
import org.micoli.php.service.intellij.psi.PhpUtil.getFirstParameterType
import org.micoli.php.symfony.messenger.service.MessengerService

class MessengerGotoDeclarationHandler : GotoDeclarationHandler {
    override fun getGotoDeclarationTargets(
        sourceElement: PsiElement?,
        offset: Int,
        editor: Editor?
    ): Array<PsiElement?>? {
        if (sourceElement == null) {
            return null
        }

        val messengerService: MessengerService = MessengerService.getInstance(sourceElement.project)
        if (messengerService.configuration?.useNativeGoToDeclaration == true) {
            return null
        }
        return handleDispatchNavigationToMessage(messengerService, sourceElement)
    }

    fun handleDispatchNavigationToMessage(
        messengerService: MessengerService,
        sourceElement: PsiElement?
    ): Array<PsiElement?>? {
        if (sourceElement !is LeafPsiElement) {
            return null
        }

        if (!messengerService.isDispatchMethod(sourceElement.text)) {
            return null
        }
        val methodRef =
            PsiTreeUtil.getParentOfType(sourceElement, MethodReference::class.java) ?: return null

        val messageClassName = getFirstParameterType(methodRef.parameters) ?: return null

        val handlers = messengerService.findHandlersByMessageName(messageClassName)

        if (handlers.isEmpty()) {
            return null
        }

        return handlers.toTypedArray()
    }
}
