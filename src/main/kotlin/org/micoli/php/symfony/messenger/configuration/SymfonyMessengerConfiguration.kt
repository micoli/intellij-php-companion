package org.micoli.php.symfony.messenger.configuration

import io.swagger.v3.oas.annotations.media.Schema

class SymfonyMessengerConfiguration {
    @Schema(description = "Disable ctrl+click to go to handler service")
    var useNativeGoToDeclaration: Boolean = false

    @Schema(description = "Root namespace for scanning classes")
    var projectRootNamespace: String = "\\App"

    @Schema(description = "Regex pattern to identify message classes")
    @JvmField
    var messageClassNamePatterns: String = ".*(Message|Command|Query|Event|Input)$"

    @Schema(description = "Interfaces that message classes implement")
    @JvmField
    var messageInterfaces: Array<String> = arrayOf()

    @Schema(description = "Interfaces that handler classes implement")
    var messageHandlerInterfaces: Array<String> =
        arrayOf(
            "Symfony\\Component\\Messenger\\Handler\\MessageHandlerInterface",
        )

    @Schema(description = "Method names used to dispatch messages")
    var dispatchMethods: Array<String> =
        arrayOf(
            "dispatch",
            "query",
            "command",
            "handle",
        )

    @Schema(description = "Method names in handler classes")
    var handlerMethods: Array<String> =
        arrayOf(
            "__invoke",
            "handle",
        )

    var asMessageHandlerAttribute: String =
        "Symfony\\Component\\Messenger\\Attribute\\AsMessageHandler"

    fun toggleUseNativeGoToDeclaration() {
        useNativeGoToDeclaration = !useNativeGoToDeclaration
    }
}
