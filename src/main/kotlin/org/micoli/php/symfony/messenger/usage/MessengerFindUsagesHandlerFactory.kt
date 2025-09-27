package org.micoli.php.symfony.messenger.usage

import com.intellij.find.findUsages.FindUsagesHandler
import com.intellij.find.findUsages.FindUsagesHandlerFactory
import com.intellij.openapi.application.ReadAction
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.PhpClass
import org.micoli.php.symfony.messenger.service.MessengerService

class MessengerFindUsagesHandlerFactory : FindUsagesHandlerFactory() {
    override fun canFindUsages(element: PsiElement): Boolean {
        return ReadAction.compute<Boolean, RuntimeException?> {
            val messengerService: MessengerService = MessengerService.getInstance(element.project)
            var isATrigger = false
            if (element is Method) {
                if (messengerService.isHandlerMethod(element.name)) {
                    isATrigger = true
                }
            }
            if (element is PhpClass) {
                isATrigger = true
            }
            if (!isATrigger) {
                return@compute false
            }

            val phpclass: PhpClass? = getMessageClass(messengerService, element)
            (phpclass != null)
        }
    }

    override fun createFindUsagesHandler(
        element: PsiElement,
        forHighlightUsages: Boolean
    ): FindUsagesHandler? {
        return ReadAction.compute<MessengerFindUsagesHandler, RuntimeException?> {
            if (canFindUsages(element)) {
                return@compute MessengerFindUsagesHandler(element)
            }
            null
        }
    }

    fun getMessageClass(messengerService: MessengerService, element: PsiElement?): PhpClass? {
        return ReadAction.compute<PhpClass?, RuntimeException?> {
            if (element is PhpClass) {
                if (messengerService.isMessageClass(element)) {
                    return@compute element
                }
                if (messengerService.isHandlerClass(element)) {
                    return@compute messengerService.getHandledMessage(element)
                }
            }
            val handlerClass =
                PsiTreeUtil.getParentOfType(element, PhpClass::class.java) ?: return@compute null
            return@compute messengerService.getHandledMessage(handlerClass)
        }
    }
}
