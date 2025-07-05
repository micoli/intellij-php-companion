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
import org.micoli.php.symfony.messenger.service.MessengerService;
import org.micoli.php.service.PhpUtil;

class MessengerFindUsagesHandler extends FindUsagesHandler {

    public MessengerFindUsagesHandler(@NotNull PsiElement psiElement) {
        super(psiElement);
    }

    @Override
    public boolean processElementUsages(@NotNull PsiElement element, @NotNull Processor<? super UsageInfo> processor, @NotNull FindUsagesOptions options) {

        if (!super.processElementUsages(element, processor, options)) {
            return false;
        }

        return processMessengerUsages(element, processor, options);
    }

    @Override
    public PsiElement @NotNull [] getPrimaryElements() {
        return new PsiElement[] { getPsiElement() };
    }

    @Override
    public PsiElement @NotNull [] getSecondaryElements() {
        return PsiElement.EMPTY_ARRAY;
    }

    private boolean processMessengerUsages(@NotNull PsiElement element, @NotNull Processor<? super UsageInfo> processor, @NotNull FindUsagesOptions options) {

        Project project = element.getProject();

        PhpClass messageClass = MessengerFindUsagesHandlerFactory.getMessageClass(element);
        if (messageClass != null) {
            return findDispatchUsages(messageClass, processor, options);
        }

        Method handlerMethod = getHandlerMethod(element);
        if (handlerMethod == null) {
            return true;
        }

        String messageClassName = MessengerService.extractMessageClassFromHandler(handlerMethod);
        if (messageClassName == null) {
            return true;
        }

        PhpClass msgClass = PhpUtil.findClassByFQN(project, messageClassName);
        if (msgClass != null) {
            return findDispatchUsages(msgClass, processor, options);
        }

        return true;
    }

    private boolean findDispatchUsages(@NotNull PhpClass messageClass, @NotNull Processor<? super UsageInfo> processor, @NotNull FindUsagesOptions options) {

        return ReadAction.compute(() -> {
            Project project = messageClass.getProject();
            Collection<MethodReference> dispatchCalls = MessengerService.findDispatchCallsForMessage(project, messageClass.getFQN());

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

    private Method getHandlerMethod(PsiElement element) {
        if (element instanceof Method method) {
            if (MessengerService.isHandlerMethod(method.getName())) {
                return method;
            }
        }

        return PsiTreeUtil.getParentOfType(element, Method.class);
    }
}
