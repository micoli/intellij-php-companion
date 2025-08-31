package org.micoli.php.symfony.messenger.configuration;

import io.swagger.v3.oas.annotations.media.Schema;

public final class SymfonyMessengerConfiguration {
    @Schema(description = "Disable ctrl+click to go to handler service")
    public boolean useNativeGoToDeclaration = false;

    @Schema(description = "Root namespace for scanning classes")
    public String projectRootNamespace = "\\App";

    @Schema(description = "Regex pattern to identify message classes")
    public String messageClassNamePatterns = ".*(Message|Command|Query|Event|Input)$";

    @Schema(description = "Interfaces that message classes implement")
    public String[] messageInterfaces = {};

    @Schema(description = "Interfaces that handler classes implement")
    public String[] messageHandlerInterfaces = {
        "Symfony\\Component\\Messenger\\Handler\\MessageHandlerInterface",
    };

    @Schema(description = "Method names used to dispatch messages")
    public String[] dispatchMethods = {
        "dispatch", "query", "command", "handle",
    };

    @Schema(description = "Method names in handler classes")
    public String[] handlerMethods = {
        "__invoke", "handle",
    };

    public String asMessageHandlerAttribute = "Symfony\\Component\\Messenger\\Attribute\\AsMessageHandler";

    public void toggleUseNativeGoToDeclaration() {
        useNativeGoToDeclaration = !useNativeGoToDeclaration;
    }
}
