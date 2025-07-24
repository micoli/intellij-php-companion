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
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;
import org.micoli.php.service.PhpUtil;
import org.micoli.php.symfony.messenger.configuration.SymfonyMessengerConfiguration;

public class MessengerService {
    private SymfonyMessengerConfiguration configuration = new SymfonyMessengerConfiguration();
    private Pattern compiledMessageClassNamePatterns = null;
    private Project project;

    public static MessengerService getInstance(Project project) {
        return project.getService(MessengerService.class);
    }

    /**
     * this can be expensive if (findHandlersByMessageName(phpClass.getProject(),phpClass.getFQN()).isEmpty()){ return
     * true; } return false:
     */
    public boolean isMessageClass(PhpClass phpClass) {
        String className = phpClass.getName();

        if (this.implementsMessageInterfaces(phpClass)) {
            return true;
        }

        return matchMessagePattern(className);
    }

    public PhpClass getHandledMessage(PhpClass handlerClass) {
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

    public @Nullable Method getInvokableMethod(PhpClass handlerClass) {
        for (String methodName : this.configuration.handlerMethods) {
            Method invokeMethod = handlerClass.findOwnMethodByName(methodName);
            if (invokeMethod != null) {
                return invokeMethod;
            }
        }
        return null;
    }

    public boolean isHandlerClass(PhpClass phpClass) {
        // Check naming pattern
        String className = phpClass.getName();
        if (matchMessagePattern(className)) {
            return true;
        }

        // Check if implements MessageHandlerInterface
        if (this.implementsMessageHandlerInterfaces(phpClass)) {
            return true;
        }

        // Check for #[AsMessageHandler] attribute
        if (this.hasHandlerAttribute(phpClass)) {
            return true;
        }

        return hasInvokableMethodsWithMessageParameter(phpClass);
    }

    public Collection<MethodReference> findDispatchCallsForMessage(String messageClassName) {
        List<MethodReference> dispatchCalls = new ArrayList<>();

        PsiSearchHelper searchHelper = PsiSearchHelper.getInstance(project);
        GlobalSearchScope scope = GlobalSearchScope.projectScope(project);

        for (String methodName : this.configuration.dispatchMethods) {
            searchHelper.processElementsWithWord(
                    (element, offsetInElement) -> {
                        // Vérifier si c'est un appel de méthode
                        MethodReference methodRef = PsiTreeUtil.getParentOfType(element, MethodReference.class);
                        if (methodRef == null || !methodName.equals(methodRef.getName())) {
                            return true;
                        }

                        if (this.isMethodCalledWithMessageInstance(methodRef, messageClassName)) {
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

    public Collection<Method> findHandlersByMessageName(String messageClassName) {
        PhpIndex phpIndex = PhpIndex.getInstance(project);

        Set<Method> handlers = new HashSet<>();

        // Method 1: Find by interface implementation
        Collection<PhpClass> interfaceHandlers = new LinkedList<>();
        for (String interfaceFQN : this.configuration.messageHandlerInterfaces) {
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
        Collection<String> allClasses =
                phpIndex.getAllClassFqns(new PlainPrefixMatcher(this.configuration.projectRootNamespace));
        for (String className : allClasses) {
            for (PhpClass phpClass : phpIndex.getClassesByFQN(PhpUtil.normalizeRootFQN(className))) {
                if (handlesMessageType(phpClass, messageClassName)) {
                    handlers.add(getInvokableMethod(phpClass));
                }
            }
        }

        return handlers;
    }

    public boolean isMethodCalledWithMessageInstance(MethodReference methodRef, String messageClassName) {
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

    private boolean hasInvokableMethodsWithMessageParameter(PhpClass phpClass) {
        Method invokeMethod = getInvokableMethod(phpClass);
        if (invokeMethod == null) {
            return false;
        }

        Parameter[] parameters = invokeMethod.getParameters();
        return parameters.length == 1 && !parameters[0].getType().toString().isEmpty();
    }

    private boolean handlesMessageType(PhpClass handler, String messageClassName) {
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

    private boolean hasInvokeMethodForMessage(PhpClass phpClass, String messageClassName) {
        if (!hasInvokableMethodsWithMessageParameter(phpClass)) {
            return false;
        }

        return handlesMessageType(phpClass, messageClassName);
    }

    public String extractMessageClassFromHandler(Method handlerMethod) {
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

    public boolean implementsMessageHandlerInterfaces(PhpClass phpClass) {
        return PhpUtil.implementsInterfaces(phpClass, this.configuration.messageHandlerInterfaces);
    }

    public boolean implementsMessageInterfaces(PhpClass phpClass) {
        return PhpUtil.implementsInterfaces(phpClass, this.configuration.messageInterfaces);
    }

    public boolean hasHandlerAttribute(PhpClass phpClass) {
        return PhpUtil.hasAttribute(phpClass, this.configuration.asMessageHandlerAttribute);
    }

    public boolean isDispatchMethod(String methodName) {
        return Arrays.asList(this.configuration.dispatchMethods).contains(methodName);
    }

    public boolean isHandlerMethod(String methodName) {
        return Arrays.asList(this.configuration.handlerMethods).contains(methodName);
    }

    public boolean matchMessagePattern(String className) {
        if (this.compiledMessageClassNamePatterns == null) {
            return false;
        }
        return this.compiledMessageClassNamePatterns.matcher(className).matches();
    }

    public Set<String> getHandledMessages(PhpClass handlerClass) {
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

    public SymfonyMessengerConfiguration getConfiguration() {
        return configuration;
    }

    public void loadConfiguration(Project project, SymfonyMessengerConfiguration symfonyMessenger) {
        this.configuration = symfonyMessenger;
        this.project = project;
        this.compiledMessageClassNamePatterns = symfonyMessenger.messageClassNamePatterns != null
                ? Pattern.compile(configuration.messageClassNamePatterns)
                : null;
    }
}
