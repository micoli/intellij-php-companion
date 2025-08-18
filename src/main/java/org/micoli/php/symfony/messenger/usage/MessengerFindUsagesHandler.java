package org.micoli.php.symfony.messenger.usage;

import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.find.findUsages.FindUsagesOptions;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.Processor;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import org.micoli.php.service.PhpUtil;
import org.micoli.php.symfony.messenger.service.MessengerService;

class MessengerFindUsagesHandler extends FindUsagesHandler {

    public MessengerFindUsagesHandler(@NotNull PsiElement psiElement) {
        super(psiElement);
    }

    @Override
    public boolean processElementUsages(
            @NotNull PsiElement element,
            @NotNull Processor<? super UsageInfo> processor,
            @NotNull FindUsagesOptions options) {

        if (!super.processElementUsages(element, processor, options)) {
            return false;
        }

        return processMessengerUsages(MessengerService.getInstance(element.getProject()), element, processor);
    }

    @Override
    public PsiElement @NotNull [] getPrimaryElements() {
        return new PsiElement[] {getPsiElement()};
    }

    @Override
    public PsiElement @NotNull [] getSecondaryElements() {
        return PsiElement.EMPTY_ARRAY;
    }

    private boolean processMessengerUsages(
            @NotNull MessengerService messengerService,
            @NotNull PsiElement element,
            @NotNull Processor<? super UsageInfo> processor) {

        Project project = element.getProject();

        PhpClass messageClass = MessengerFindUsagesHandlerFactory.getMessageClass(messengerService, element);
        if (messageClass != null) {
            return findDispatchUsages(messengerService, messageClass, processor);
        }

        Method handlerMethod = getHandlerMethod(messengerService, element);
        if (handlerMethod == null) {
            return true;
        }

        String messageClassName = messengerService.extractMessageClassFromHandler(handlerMethod);
        if (messageClassName == null) {
            return true;
        }

        PhpClass msgClass = PhpUtil.findClassByFQN(project, messageClassName);
        if (msgClass != null) {
            return findDispatchUsages(messengerService, msgClass, processor);
        }

        return true;
    }

    private boolean findDispatchUsages(
            @NotNull MessengerService messengerService,
            @NotNull PhpClass messageClass,
            @NotNull Processor<? super UsageInfo> processor) {

        return ReadAction.compute(() -> {
            Collection<MethodReference> dispatchCalls =
                    messengerService.findDispatchCallsForMessage(messageClass.getFQN());

            for (MethodReference dispatchCall : dispatchCalls) {
                if (!dispatchCall.isValid()) {
                    continue;
                }

                UsageInfo usageInfo = new UsageInfo((PsiElement) dispatchCall);
                if (!processor.process(usageInfo)) {
                    return false;
                }
            }

            return true;
        });
    }

    private Method getHandlerMethod(MessengerService messengerService, PsiElement element) {
        if (element instanceof Method method) {
            if (messengerService.isHandlerMethod(method.getName())) {
                return method;
            }
        }

        return PsiTreeUtil.getParentOfType(element, Method.class);
    }
}
