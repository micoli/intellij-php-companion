package org.micoli.php.symfony.messenger.service;

import java.util.regex.Pattern;
import org.micoli.php.symfony.messenger.configuration.SymfonyMessengerConfiguration;

public class MessengerServiceConfiguration {
    private static String projectRootNamespace = "\\App";
    private static String messageClassNamePatterns = ".*(Message|Command|Query|Event|Input)$";
    private static Pattern compiledMessageClassNamePatterns = Pattern.compile(messageClassNamePatterns);

    private static String[] messageInterfaces = {};
    private static String[] messageHandlerInterfaces = { "Symfony\\Component\\Messenger\\Handler\\MessageHandlerInterface", };
    public static String[] dispatchMethods = { "dispatch", "query", "command", "handle", };
    public static String[] handlerMethods = { "__invoke", "handle", };
    private static final String AS_MESSAGE_HANDLER_ATTRIBUTE = "Symfony\\Component\\Messenger\\Attribute\\AsMessageHandler";

    public static void loadConfiguration(SymfonyMessengerConfiguration symfonyMessengerConfiguration) {
        if (symfonyMessengerConfiguration.projectRootNamespace != null) {
            setProjectRootNamespace(symfonyMessengerConfiguration.projectRootNamespace);
        }
        if (symfonyMessengerConfiguration.messageClassNamePatterns != null) {
            setMessageClassNamePatterns(symfonyMessengerConfiguration.messageClassNamePatterns);
        }
        if (symfonyMessengerConfiguration.messageHandlerInterfaces != null) {
            setMessageHandlerInterfaces(symfonyMessengerConfiguration.messageHandlerInterfaces);
        }
        if (symfonyMessengerConfiguration.messageInterfaces != null) {
            setMessageInterfaces(symfonyMessengerConfiguration.messageInterfaces);
        }
        if (symfonyMessengerConfiguration.dispatchMethods != null) {
            setDispatchMethods(symfonyMessengerConfiguration.dispatchMethods);
        }
        if (symfonyMessengerConfiguration.handlerMethods != null) {
            setHandlerMethods(symfonyMessengerConfiguration.handlerMethods);
        }
    }

    public static String getProjectRootNamespace() {
        return projectRootNamespace;
    }

    public static Pattern getCompiledMessageClassNamePatterns() {
        return compiledMessageClassNamePatterns;
    }

    public static String[] getMessageInterfaces() {
        return messageInterfaces;
    }

    public static String[] getMessageHandlerInterfaces() {
        return messageHandlerInterfaces;
    }

    public static String[] getDispatchMethods() {
        return dispatchMethods;
    }

    public static String[] getHandlerMethods() {
        return handlerMethods;
    }

    public static String getAsMessageHandlerAttribute() {
        return AS_MESSAGE_HANDLER_ATTRIBUTE;
    }

    public static void setProjectRootNamespace(String projectRootNamespace) {
        MessengerServiceConfiguration.projectRootNamespace = projectRootNamespace;
    }

    public static void setMessageClassNamePatterns(String patterns) {
        messageClassNamePatterns = patterns;
        compiledMessageClassNamePatterns = Pattern.compile(patterns);
    }

    public static void setMessageInterfaces(String[] interfaces) {
        messageInterfaces = interfaces;
    }

    public static void setMessageHandlerInterfaces(String[] interfaces) {
        messageHandlerInterfaces = interfaces;
    }

    public static void setDispatchMethods(String[] methods) {
        dispatchMethods = methods;
    }

    public static void setHandlerMethods(String[] methods) {
        handlerMethods = methods;
    }
}
