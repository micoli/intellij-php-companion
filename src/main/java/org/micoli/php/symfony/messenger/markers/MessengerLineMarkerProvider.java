package org.micoli.php.symfony.messenger.markers;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.IconLoader;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.micoli.php.service.intellij.psi.PhpUtil;
import org.micoli.php.service.intellij.psi.PsiElementUtil;
import org.micoli.php.symfony.messenger.service.MessengerService;
import org.micoli.php.ui.Notification;
import org.micoli.php.ui.popup.*;

public class MessengerLineMarkerProvider implements LineMarkerProvider {

    Icon navigateSendIcon = IconLoader.getIcon("icons/messenger-send-2.svg", MessengerLineMarkerProvider.class);
    Icon navigateReceiveIcon = IconLoader.getIcon("icons/messenger-receive-2.svg", MessengerLineMarkerProvider.class);

    public MessengerLineMarkerProvider() {}

    @Override
    public @Nullable LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(
            @NotNull List<? extends PsiElement> elements, @NotNull Collection<? super LineMarkerInfo<?>> result) {
        if (elements.isEmpty()) {
            return;
        }
        MessengerService messengerService =
                MessengerService.getInstance(elements.getFirst().getProject());
        for (PsiElement element : elements) {
            if (element instanceof MethodReference methodRef) {
                processDispatchMethod(messengerService, methodRef, result);
            }
            if (element instanceof Method method) {
                processHandleMethod(messengerService, method, result);
            }
        }
    }

    private void processDispatchMethod(
            MessengerService messengerService,
            MethodReference methodRef,
            Collection<? super LineMarkerInfo<?>> result) {
        if (methodRef == null || !messengerService.isDispatchMethod(methodRef.getName())) {
            return;
        }

        String messageClassName = PhpUtil.getFirstParameterType(methodRef.getParameters());

        if (messageClassName == null) {
            return;
        }

        Collection<Method> handlers = messengerService.findHandlersByMessageName(messageClassName);

        if (handlers.isEmpty()) {
            return;
        }

        result.add(NavigationGutterIconBuilder.create(navigateSendIcon)
                .setTargets(handlers)
                .setTooltipText("Navigate to message handlers")
                .createLineMarkerInfo(PsiElementUtil.findFirstLeafElement(methodRef)));
    }

    private void processHandleMethod(
            MessengerService messengerService, Method method, Collection<? super LineMarkerInfo<?>> result) {
        if (method == null) {
            return;
        }

        if (!messengerService.isHandlerMethod(method.getName())) {
            return;
        }

        String messageClassName = messengerService.extractMessageClassFromHandler(method);
        if (messageClassName == null) {
            return;
        }

        Project project = method.getProject();
        PhpClass msgClass = PhpUtil.findClassByFQN(project, PhpUtil.normalizeNonRootFQN(messageClassName));
        if (msgClass == null) {
            return;
        }
        if (messengerService.isMessageClass(msgClass)) {
            PsiElement leafElement = PsiElementUtil.findFirstLeafElement(method);

            result.add(new LineMarkerInfo<>(
                    leafElement,
                    leafElement.getTextRange(),
                    navigateReceiveIcon,
                    psiElement -> "Search for usages of [" + messageClassName + "]",
                    (mouseEvent, elt) ->
                            navigateToMessageDispatchCalls(messengerService, mouseEvent, project, messageClassName),
                    GutterIconRenderer.Alignment.CENTER,
                    () -> "Search for usages of [" + messageClassName + "]"));
        }
    }

    private void navigateToMessageDispatchCalls(
            MessengerService messengerService, MouseEvent mouseEvent, Project project, String messageClassName) {
        messengerService.findDispatchCallsForMessageAsync(messageClassName, dispatchCalls -> {
            ArrayList<PsiElement> elements = new ArrayList<>();
            ApplicationManager.getApplication().runReadAction(() -> {
                for (MethodReference dispatchCall : dispatchCalls) {
                    if (!dispatchCall.isValid()) {
                        continue;
                    }
                    elements.add(dispatchCall);
                }
            });

            ApplicationManager.getApplication().invokeLater(() -> {
                if (elements.isEmpty()) {
                    Notification.getInstance(project).error("No usage found");
                    return;
                }
                if (elements.size() == 1) {
                    if (elements.getFirst() instanceof Navigatable) {
                        ((Navigatable) elements.getFirst()).navigate(true);
                    }
                    return;
                }
                List<NavigableItem> navigableItemList = elements.stream()
                        .map(psiElement -> ApplicationManager.getApplication()
                                .runReadAction((Computable<NavigableItem>) () -> {
                                    PsiFile containingFile = psiElement.getContainingFile();
                                    if (containingFile == null) {
                                        return null;
                                    }
                                    if (!((Navigatable) psiElement).canNavigate()) {
                                        return null;
                                    }

                                    FileExtract fileExtract = PsiElementUtil.getFileExtract(psiElement, 0);
                                    return new NavigableItem(
                                            fileExtract, (Navigatable) psiElement, psiElement.getIcon(0));
                                }))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toCollection(ArrayList::new));

                if (!hasMultipleFilesReferenced(navigableItemList)) {
                    NavigableListPopup.showNavigablePopup(
                            mouseEvent,
                            navigableItemList.stream()
                                    .map(item -> (NavigableListPopupItem) item)
                                    .toList());
                    return;
                }
                NavigableListPopup.showNavigablePopup(
                        mouseEvent, addActionsToItems(project, messageClassName, navigableItemList));
            });
        });
    }

    private @NotNull List<NavigableListPopupItem> addActionsToItems(
            Project project, String messageClassName, List<NavigableItem> navigableItemList) {
        List<Navigatable> navigatableList =
                navigableItemList.stream().map(NavigableItem::getNavigable).toList();
        List<NavigableListPopupItem> finalList = new ArrayList<>(navigableItemList);
        finalList.add(new NavigableOpenAllAction(navigatableList));
        finalList.add(new NavigableOpenSearchAction(
                project,
                navigatableList,
                String.format("Find message dispatch calls %s", messageClassName),
                messageClassName));
        return finalList;
    }

    private static boolean hasMultipleFilesReferenced(List<NavigableItem> navigableItemList) {
        return navigableItemList.stream()
                        .map(navigableItem -> navigableItem.getFileExtract().file())
                        .distinct()
                        .count()
                > 1;
    }
}
