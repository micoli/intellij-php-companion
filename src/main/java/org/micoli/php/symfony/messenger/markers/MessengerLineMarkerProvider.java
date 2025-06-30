package org.micoli.php.symfony.messenger.markers;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.micoli.php.symfony.messenger.service.MessengerService;
import org.micoli.php.symfony.messenger.service.PHPHelper;

import javax.swing.*;
import java.util.Collection;
import java.util.List;

public class MessengerLineMarkerProvider implements LineMarkerProvider {

    Icon navigateIcon = IconLoader.getIcon("icons/messenger-send-2.svg", MessengerLineMarkerProvider.class);

    public MessengerLineMarkerProvider() {
    }

    @Override
    public @Nullable LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<? extends PsiElement> elements, @NotNull Collection<? super LineMarkerInfo<?>> result) {
        for (PsiElement element : elements) {
            if (element instanceof MethodReference methodRef) {
                processDispatchMethod(methodRef, result);
            }
        }
    }

    private void processDispatchMethod(MethodReference methodRef, Collection<? super LineMarkerInfo<?>> result) {
        if (methodRef == null || !MessengerService.isDispatchMethod(methodRef.getName())) {
            return;
        }

        String messageClassName = PHPHelper.getFirstParameterType(methodRef.getParameters());

        if (messageClassName == null) {
            return;
        }

        Collection<Method> handlers = MessengerService.findHandlersByMessageName(methodRef.getProject(), messageClassName);

        if (handlers.isEmpty()) {
            return;
        }

        for(Method targetElement : handlers) {
            result.add(NavigationGutterIconBuilder.create(navigateIcon).setTargets(targetElement).setTooltipText("Navigate to [" + targetElement.getFQN() + "]").createLineMarkerInfo(methodRef));
        }
    }
}
