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
            boolean isATrigger = false;
            if (element instanceof Method method) {
                if (MessengerService.isHandlerMethod(method.getName())) {
                    isATrigger = true;
                }
            }
            if (element instanceof PhpClass) {
                isATrigger = true;
            }
            if (!isATrigger) {
                return false;
            }

            PhpClass phpclass = getMessageClass(element);
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

    public static PhpClass getMessageClass(PsiElement element) {
        return ReadAction.compute(() -> {
            if (element instanceof PhpClass phpClass) {
                if (MessengerService.isMessageClass(phpClass)) {
                    return phpClass;
                }
                if (MessengerService.isHandlerClass(phpClass)) {
                    return MessengerService.getHandledMessage(phpClass);
                }
            }

            PhpClass handlerClass = PsiTreeUtil.getParentOfType(element, PhpClass.class);
            if (handlerClass == null) {
                return null;
            }
            return MessengerService.getHandledMessage(handlerClass);
        });
    }
}
