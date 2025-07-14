package org.micoli.php.symfony.messenger.service;

import com.intellij.codeInsight.completion.PlainPrefixMatcher;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import java.util.*;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;
import org.micoli.php.service.PhpUtil;

public class MessengerService {

    /**
     * this can be expensive if (findHandlersByMessageName(phpClass.getProject(),phpClass.getFQN()).isEmpty()){ return
     * true; } return false:
     */
    public static boolean isMessageClass(PhpClass phpClass) {
        String className = phpClass.getName();

        if (MessengerService.implementsMessageInterfaces(phpClass)) {
            return true;
        }

        return matchMessagePattern(className);
    }

    public static PhpClass getHandledMessage(PhpClass handlerClass) {
        Method invokeMethod = getInvokableMethod(handlerClass);
        if (invokeMethod == null) {
            return null;
        }

        Parameter[] parameters = invokeMethod.getParameters();
        if (parameters.length != 1) {
            return null;
        }

        PhpType parameterType = parameters[0].getType();
        for (String type : parameterType.getTypes()) {
            if (type.startsWith("\\")) {
                return PhpUtil.getPhpClassByFQN(handlerClass.getProject(), type);
            }
        }
        return null;
    }

    public static @Nullable Method getInvokableMethod(PhpClass handlerClass) {
        for (String methodName : MessengerServiceConfiguration.getHandlerMethods()) {
            Method invokeMethod = handlerClass.findOwnMethodByName(methodName);
            if (invokeMethod != null) {
                return invokeMethod;
            }
        }
        return null;
    }

    public static boolean isHandlerClass(PhpClass phpClass) {
        // Check naming pattern
        String className = phpClass.getName();
        if (matchMessagePattern(className)) {
            return true;
        }

        // Check if implements MessageHandlerInterface
        if (MessengerService.implementsMessageHandlerInterfaces(phpClass)) {
            return true;
        }

        // Check for #[AsMessageHandler] attribute
        if (MessengerService.hasHandlerAttribute(phpClass)) {
            return true;
        }

        return hasInvokableMethodsWithMessageParameter(phpClass);
    }

    public static Collection<MethodReference> findDispatchCallsForMessage(Project project, String messageClassName) {
        List<MethodReference> dispatchCalls = new ArrayList<>();

        PsiSearchHelper searchHelper = PsiSearchHelper.getInstance(project);
        GlobalSearchScope scope = GlobalSearchScope.projectScope(project);

        for (String methodName : MessengerServiceConfiguration.getDispatchMethods()) {
            searchHelper.processElementsWithWord(
                    (element, offsetInElement) -> {
                        // Vérifier si c'est un appel de méthode
                        MethodReference methodRef = PsiTreeUtil.getParentOfType(element, MethodReference.class);
                        if (methodRef == null || !methodName.equals(methodRef.getName())) {
                            return true;
                        }

                        if (MessengerService.isMethodCalledWithMessageInstance(methodRef, messageClassName)) {
                            dispatchCalls.add(methodRef);
                        }

                        return true;
                    },
                    scope,
                    methodName,
                    UsageSearchContext.IN_CODE,
                    true,
                    false);
        }

        return dispatchCalls.stream().distinct().collect(Collectors.toList());
    }

    public static Collection<Method> findHandlersByMessageName(Project project, String messageClassName) {
        PhpIndex phpIndex = PhpIndex.getInstance(project);

        Set<Method> handlers = new HashSet<>();

        // Method 1: Find by interface implementation
        Collection<PhpClass> interfaceHandlers = new LinkedList<>();
        for (String interfaceFQN : MessengerServiceConfiguration.getMessageHandlerInterfaces()) {
            PhpIndex.getInstance(project).processAllSubclasses(interfaceFQN, phpClass -> {
                interfaceHandlers.add(phpClass);
                return true;
            });
        }
        for (PhpClass handler : interfaceHandlers) {
            if (handlesMessageType(handler, messageClassName)) {
                handlers.add(getInvokableMethod(handler));
            }
        }

        // Method 2: Scan all classes for __invoke method with right parameter
        // (This is expensive, so we might want to cache results)
        Collection<String> allClasses = phpIndex.getAllClassFqns(
                new PlainPrefixMatcher(MessengerServiceConfiguration.getProjectRootNamespace()));
        for (String className : allClasses) {
            for (PhpClass phpClass : phpIndex.getClassesByFQN(PhpUtil.normalizeRootFQN(className))) {
                if (handlesMessageType(phpClass, messageClassName)) {
                    handlers.add(getInvokableMethod(phpClass));
                }
            }
        }

        return handlers;
    }

    public static boolean isMethodCalledWithMessageInstance(MethodReference methodRef, String messageClassName) {
        messageClassName = PhpUtil.normalizeRootFQN(messageClassName);

        PsiElement[] parameters = methodRef.getParameters();
        if (parameters.length == 0) {
            return false;
        }
        PsiElement firstParam = parameters[0];

        if (firstParam instanceof NewExpression newExpr) {
            ClassReference classRef = newExpr.getClassReference();
            if (classRef != null) {
                return messageClassName.equals(classRef.getFQN());
            }
        }

        if (firstParam instanceof Variable variable) {
            return variable.getType().toString().contains(messageClassName);
        }

        if (firstParam instanceof MethodReference paramMethodRef) {
            return paramMethodRef.getType().toString().contains(messageClassName);
        }

        if (firstParam instanceof FieldReference fieldRef) {
            return fieldRef.getType().toString().contains(messageClassName);
        }

        return false;
    }

    private static boolean hasInvokableMethodsWithMessageParameter(PhpClass phpClass) {
        Method invokeMethod = getInvokableMethod(phpClass);
        if (invokeMethod == null) {
            return false;
        }

        Parameter[] parameters = invokeMethod.getParameters();
        return parameters.length == 1 && !parameters[0].getType().toString().isEmpty();
    }

    private static boolean handlesMessageType(PhpClass handler, String messageClassName) {
        Method invokeMethod = getInvokableMethod(handler);
        if (invokeMethod == null) {
            return false;
        }

        Parameter[] parameters = invokeMethod.getParameters();
        if (parameters.length != 1) {
            return false;
        }

        PhpType paramType = parameters[0].getType();
        return paramType.toString().contains(messageClassName);
    }

    private static boolean hasInvokeMethodForMessage(PhpClass phpClass, String messageClassName) {
        if (!hasInvokableMethodsWithMessageParameter(phpClass)) {
            return false;
        }

        return handlesMessageType(phpClass, messageClassName);
    }

    public static String extractMessageClassFromHandler(Method handlerMethod) {
        Parameter[] parameters = handlerMethod.getParameters();
        if (parameters.length == 0) {
            return null;
        }

        Parameter firstParam = parameters[0];
        PhpType paramType = firstParam.getType();

        if (!paramType.isEmpty()) {
            return paramType.toString();
        }

        return null;
    }

    public static boolean implementsMessageHandlerInterfaces(PhpClass phpClass) {
        return PhpUtil.implementsInterfaces(phpClass, MessengerServiceConfiguration.getMessageHandlerInterfaces());
    }

    public static boolean implementsMessageInterfaces(PhpClass phpClass) {
        return PhpUtil.implementsInterfaces(phpClass, MessengerServiceConfiguration.getMessageInterfaces());
    }

    public static boolean hasHandlerAttribute(PhpClass phpClass) {
        return PhpUtil.hasAttribute(phpClass, MessengerServiceConfiguration.getAsMessageHandlerAttribute());
    }

    public static boolean isDispatchMethod(String methodName) {
        return Arrays.asList(MessengerServiceConfiguration.getDispatchMethods()).contains(methodName);
    }

    public static boolean isHandlerMethod(String methodName) {
        return Arrays.asList(MessengerServiceConfiguration.getHandlerMethods()).contains(methodName);
    }

    public static boolean matchMessagePattern(String className) {
        if (MessengerServiceConfiguration.getCompiledMessageClassNamePatterns() == null) {
            return false;
        }
        return MessengerServiceConfiguration.getCompiledMessageClassNamePatterns()
                .matcher(className)
                .matches();
    }

    public static Set<String> getHandledMessages(PhpClass handlerClass) {
        Set<String> messages = new HashSet<>();

        Method invokeMethod = getInvokableMethod(handlerClass);
        if (invokeMethod == null) {
            return messages;
        }

        Parameter[] parameters = invokeMethod.getParameters();
        for (Parameter param : parameters) {
            String paramType = param.getType().toString();
            if (!paramType.isEmpty() && !paramType.equals("mixed")) {
                messages.add(paramType);
            }
        }
        return messages;
    }
}
