package org.micoli.php.symfony.messenger.configuration;

public final class SymfonyMessengerConfiguration {
    public String projectRootNamespace = "\\App";
    public String messageClassNamePatterns = ".*(Message|Command|Query|Event|Input)$";
    public String[] messageInterfaces = {};
    public String[] messageHandlerInterfaces = {
        "Symfony\\Component\\Messenger\\Handler\\MessageHandlerInterface",
    };
    public String[] dispatchMethods = {
        "dispatch", "query", "command", "handle",
    };
    public String[] handlerMethods = {
        "__invoke", "handle",
    };
    public String asMessageHandlerAttribute = "Symfony\\Component\\Messenger\\Attribute\\AsMessageHandler";
}
