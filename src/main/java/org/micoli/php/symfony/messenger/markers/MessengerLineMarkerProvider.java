package org.micoli.php.symfony.messenger.markers;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.micoli.php.service.PhpUtil;
import org.micoli.php.service.PsiElementUtil;
import org.micoli.php.symfony.messenger.service.MessengerService;
import org.micoli.php.ui.Notification;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MessengerLineMarkerProvider implements LineMarkerProvider {

    Icon navigateSendIcon = IconLoader.getIcon("icons/messenger-send-2.svg", MessengerLineMarkerProvider.class);
    Icon navigateReceiveIcon = IconLoader.getIcon("icons/messenger-receive-2.svg", MessengerLineMarkerProvider.class);

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
            if (element instanceof Method method) {
                processHandleMethod(method, result);
            }
        }
    }

    private void processDispatchMethod(MethodReference methodRef, Collection<? super LineMarkerInfo<?>> result) {
        if (methodRef == null || !MessengerService.isDispatchMethod(methodRef.getName())) {
            return;
        }

        String messageClassName = PhpUtil.getFirstParameterType(methodRef.getParameters());

        if (messageClassName == null) {
            return;
        }

        Collection<Method> handlers = MessengerService.findHandlersByMessageName(methodRef.getProject(), messageClassName);

        if (handlers.isEmpty()) {
            return;
        }

        result.add(NavigationGutterIconBuilder.create(navigateSendIcon).setTargets(handlers).setTooltipText("Navigate to message handlers").createLineMarkerInfo(PsiElementUtil.findFirstLeafElement(methodRef)));
    }

    private void processHandleMethod(Method method, Collection<? super LineMarkerInfo<?>> result) {
        if (method == null) {
            return;
        }

        if (!MessengerService.isHandlerMethod(method.getName())) {
            return;
        }

        String messageClassName = MessengerService.extractMessageClassFromHandler(method);
        if (messageClassName == null) {
            return;
        }

        Project project = method.getProject();
        PhpClass msgClass = PhpUtil.findClassByFQN(project, PhpUtil.normalizeNonRootFQN(messageClassName));
        if (msgClass == null) {
            return;
        }
        if (MessengerService.isMessageClass(msgClass)) {
            PsiElement leafElement = PsiElementUtil.findFirstLeafElement(method);
            result.add(new LineMarkerInfo<>(leafElement, leafElement.getTextRange(), navigateReceiveIcon, psiElement -> "Search for usages of [" + messageClassName + "]", (mouseEvent, elt) -> navigateToMessageDispatchCalls(mouseEvent, project, messageClassName), GutterIconRenderer.Alignment.CENTER, () -> "Search for usages of [" + messageClassName + "]"));
        }
    }

    private static void navigateToMessageDispatchCalls(MouseEvent mouseEvent, Project project, String messageClassName) {
        Collection<MethodReference> dispatchCalls = MessengerService.findDispatchCallsForMessage(project, messageClassName);
        ArrayList<PsiElement> elements = new ArrayList<>();
        for (MethodReference dispatchCall : dispatchCalls) {
            if (!dispatchCall.isValid()) {
                continue;
            }
            elements.add(dispatchCall);
        }
        if (elements.isEmpty()) {
            Notification.error("No usage found");
            return;
        }
        if (elements.size() == 1) {
            if (elements.getFirst() instanceof Navigatable) {
                ((Navigatable) elements.getFirst()).navigate(true);
            }
            return;
        }
        PsiElementsPopup.showLinksToElementsPopup(mouseEvent, elements);
    }

}
