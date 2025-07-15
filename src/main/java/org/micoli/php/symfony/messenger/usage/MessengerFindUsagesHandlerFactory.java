package org.micoli.php.symfony.messenger.usage;

import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.find.findUsages.FindUsagesHandlerFactory;
import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.micoli.php.symfony.messenger.service.MessengerService;

public class MessengerFindUsagesHandlerFactory extends FindUsagesHandlerFactory {

    @Override
    public boolean canFindUsages(@NotNull PsiElement element) {
        return ReadAction.compute(() -> {
            MessengerService messengerService = MessengerService.getInstance(element.getProject());
            boolean isATrigger = false;
            if (element instanceof Method method) {
                if (messengerService.isHandlerMethod(method.getName())) {
                    isATrigger = true;
                }
            }
            if (element instanceof PhpClass) {
                isATrigger = true;
            }
            if (!isATrigger) {
                return false;
            }

            PhpClass phpclass = getMessageClass(messengerService, element);
            return (phpclass != null);
        });
    }

    @Override
    public @Nullable FindUsagesHandler createFindUsagesHandler(
            @NotNull PsiElement element, boolean forHighlightUsages) {
        return ReadAction.compute(() -> {
            if (canFindUsages(element)) {
                return new MessengerFindUsagesHandler(element);
            }
            return null;
        });
    }

    public static PhpClass getMessageClass(MessengerService messengerService, PsiElement element) {
        return ReadAction.compute(() -> {
            if (element instanceof PhpClass phpClass) {

                if (messengerService.isMessageClass(phpClass)) {
                    return phpClass;
                }
                if (messengerService.isHandlerClass(phpClass)) {
                    return messengerService.getHandledMessage(phpClass);
                }
            }

            PhpClass handlerClass = PsiTreeUtil.getParentOfType(element, PhpClass.class);
            if (handlerClass == null) {
                return null;
            }
            return messengerService.getHandledMessage(handlerClass);
        });
    }
}
