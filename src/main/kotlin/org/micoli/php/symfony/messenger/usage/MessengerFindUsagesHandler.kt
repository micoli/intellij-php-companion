package org.micoli.php.symfony.messenger.usage

import com.intellij.find.findUsages.FindUsagesHandler
import com.intellij.find.findUsages.FindUsagesOptions
import com.intellij.openapi.application.ReadAction
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.usageView.UsageInfo
import com.intellij.util.Processor
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.PhpClass
import org.micoli.php.service.intellij.psi.PhpUtil.findClassByFQN
import org.micoli.php.symfony.messenger.service.MessengerService

internal class MessengerFindUsagesHandler(psiElement: PsiElement) : FindUsagesHandler(psiElement) {
    override fun processElementUsages(
        element: PsiElement,
        processor: Processor<in UsageInfo?>,
        options: FindUsagesOptions
    ): Boolean {
        if (!super.processElementUsages(element, processor, options)) {
            return false
        }

        return processMessengerUsages(
            MessengerService.getInstance(element.project), element, processor)
    }

    override fun getPrimaryElements(): Array<PsiElement?> {
        return arrayOf(psiElement)
    }

    override fun getSecondaryElements(): Array<PsiElement?> {
        return PsiElement.EMPTY_ARRAY
    }

    private fun processMessengerUsages(
        messengerService: MessengerService,
        element: PsiElement,
        processor: Processor<in UsageInfo?>
    ): Boolean {
        val project = element.project

        val messageClass: PhpClass? =
            MessengerFindUsagesHandlerFactory().getMessageClass(messengerService, element)
        if (messageClass != null) {
            return findDispatchUsages(messengerService, messageClass, processor)
        }

        val handlerMethod = getHandlerMethod(messengerService, element) ?: return true

        val messageClassName =
            messengerService.extractMessageClassFromHandler(handlerMethod) ?: return true

        val msgClass = findClassByFQN(project, messageClassName)
        if (msgClass != null) {
            return findDispatchUsages(messengerService, msgClass, processor)
        }

        return true
    }

    private fun findDispatchUsages(
        messengerService: MessengerService,
        messageClass: PhpClass,
        processor: Processor<in UsageInfo?>
    ): Boolean {
        return ReadAction.compute<Boolean, RuntimeException?> {
            val dispatchCalls = messengerService.findDispatchCallsForMessage(messageClass.fqn)
            for (dispatchCall in dispatchCalls) {
                if (dispatchCall?.isValid == true) {
                    continue
                }

                val usageInfo = UsageInfo(dispatchCall as PsiElement)
                if (!processor.process(usageInfo)) {
                    return@compute false
                }
            }
            true
        }
    }

    private fun getHandlerMethod(
        messengerService: MessengerService,
        element: PsiElement?
    ): Method? {
        if (element is Method) {
            if (messengerService.isHandlerMethod(element.name)) {
                return element
            }
        }

        return PsiTreeUtil.getParentOfType(element, Method::class.java)
    }
}
