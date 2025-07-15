package org.micoli.php.symfony.messenger.service;

import java.util.regex.Pattern;
import org.micoli.php.symfony.messenger.configuration.SymfonyMessengerConfiguration;

public class MessengerServiceConfiguration {
    private String projectRootNamespace = "\\App";
    private String messageClassNamePatterns = ".*(Message|Command|Query|Event|Input)$";
    private Pattern compiledMessageClassNamePatterns = Pattern.compile(messageClassNamePatterns);

    private String[] messageInterfaces = {};
    private String[] messageHandlerInterfaces = {
        "Symfony\\Component\\Messenger\\Handler\\MessageHandlerInterface",
    };
    public String[] dispatchMethods = {
        "dispatch", "query", "command", "handle",
    };
    public String[] handlerMethods = {
        "__invoke", "handle",
    };
    private final String AS_MESSAGE_HANDLER_ATTRIBUTE = "Symfony\\Component\\Messenger\\Attribute\\AsMessageHandler";

    public static MessengerServiceConfiguration create(SymfonyMessengerConfiguration symfonyMessengerConfiguration) {
        MessengerServiceConfiguration instance = new MessengerServiceConfiguration();
        if (symfonyMessengerConfiguration == null) {
            return null;
        }
        if (symfonyMessengerConfiguration.projectRootNamespace != null) {
            instance.setProjectRootNamespace(symfonyMessengerConfiguration.projectRootNamespace);
        }
        if (symfonyMessengerConfiguration.messageClassNamePatterns != null) {
            instance.setMessageClassNamePatterns(symfonyMessengerConfiguration.messageClassNamePatterns);
        }
        if (symfonyMessengerConfiguration.messageHandlerInterfaces != null) {
            instance.setMessageHandlerInterfaces(symfonyMessengerConfiguration.messageHandlerInterfaces);
        }
        if (symfonyMessengerConfiguration.messageInterfaces != null) {
            instance.setMessageInterfaces(symfonyMessengerConfiguration.messageInterfaces);
        }
        if (symfonyMessengerConfiguration.dispatchMethods != null) {
            instance.setDispatchMethods(symfonyMessengerConfiguration.dispatchMethods);
        }
        if (symfonyMessengerConfiguration.handlerMethods != null) {
            instance.setHandlerMethods(symfonyMessengerConfiguration.handlerMethods);
        }

        return instance;
    }

    public String getProjectRootNamespace() {
        return projectRootNamespace;
    }

    public Pattern getCompiledMessageClassNamePatterns() {
        return compiledMessageClassNamePatterns;
    }

    public String[] getMessageInterfaces() {
        return messageInterfaces;
    }

    public String[] getMessageHandlerInterfaces() {
        return messageHandlerInterfaces;
    }

    public String[] getDispatchMethods() {
        return dispatchMethods;
    }

    public String[] getHandlerMethods() {
        return handlerMethods;
    }

    public String getAsMessageHandlerAttribute() {
        return AS_MESSAGE_HANDLER_ATTRIBUTE;
    }

    public void setProjectRootNamespace(String projectRootNamespace) {
        this.projectRootNamespace = projectRootNamespace;
    }

    public void setMessageClassNamePatterns(String patterns) {
        messageClassNamePatterns = patterns;
        compiledMessageClassNamePatterns = Pattern.compile(patterns);
    }

    public void setMessageInterfaces(String[] interfaces) {
        messageInterfaces = interfaces;
    }

    public void setMessageHandlerInterfaces(String[] interfaces) {
        messageHandlerInterfaces = interfaces;
    }

    public void setDispatchMethods(String[] methods) {
        dispatchMethods = methods;
    }

    public void setHandlerMethods(String[] methods) {
        handlerMethods = methods;
    }
}
