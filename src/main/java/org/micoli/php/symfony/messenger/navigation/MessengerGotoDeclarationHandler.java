package org.micoli.php.symfony.messenger.navigation;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import java.util.Collection;
import org.jetbrains.annotations.Nullable;
import org.micoli.php.symfony.messenger.service.MessengerService;
import org.micoli.php.service.PhpUtil;

public class MessengerGotoDeclarationHandler implements GotoDeclarationHandler {

    @Override
    public PsiElement @Nullable [] getGotoDeclarationTargets(@Nullable PsiElement sourceElement, int offset, Editor editor) {

        if (sourceElement == null) {
            return null;
        }

        return handleDispatchNavigationToMessage(sourceElement);
    }

    private @Nullable PsiElement[] handleDispatchNavigationToMessage(PsiElement sourceElement) {
        MethodReference methodRef = PsiTreeUtil.getParentOfType(sourceElement, MethodReference.class);

        if (methodRef == null || !MessengerService.isDispatchMethod(methodRef.getName())) {
            return null;
        }

        String messageClassName = PhpUtil.getFirstParameterType(methodRef.getParameters());

        if (messageClassName == null) {
            return null;
        }

        Collection<Method> handlers = MessengerService.findHandlersByMessageName(sourceElement.getProject(), messageClassName);

        if (handlers.isEmpty()) {
            return null;
        }

        return handlers.toArray(PsiElement[]::new);
    }
}
