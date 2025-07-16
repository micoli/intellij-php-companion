package org.micoli.php.symfony.messenger.navigation;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import java.util.Collection;
import org.jetbrains.annotations.Nullable;
import org.micoli.php.service.PhpUtil;
import org.micoli.php.symfony.messenger.service.MessengerService;

public class MessengerGotoDeclarationHandler implements GotoDeclarationHandler {

    @Override
    public PsiElement @Nullable [] getGotoDeclarationTargets(
            @Nullable PsiElement sourceElement, int offset, Editor editor) {

        if (sourceElement == null) {
            return null;
        }

        return handleDispatchNavigationToMessage(
                MessengerService.getInstance(sourceElement.getProject()), sourceElement);
    }

    public @Nullable PsiElement[] handleDispatchNavigationToMessage(
            MessengerService messengerService, PsiElement sourceElement) {

        if (!(sourceElement instanceof LeafPsiElement element)) {
            return null;
        }

        if (!messengerService.isDispatchMethod(element.getText())) {
            return null;
        }
        MethodReference methodRef = PsiTreeUtil.getParentOfType(sourceElement, MethodReference.class);

        if (methodRef == null) {
            return null;
        }

        String messageClassName = PhpUtil.getFirstParameterType(methodRef.getParameters());

        if (messageClassName == null) {
            return null;
        }

        Collection<Method> handlers =
                messengerService.findHandlersByMessageName(sourceElement.getProject(), messageClassName);

        if (handlers.isEmpty()) {
            return null;
        }

        return handlers.toArray(Method[]::new);
    }
}
