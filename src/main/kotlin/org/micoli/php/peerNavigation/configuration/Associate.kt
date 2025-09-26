package org.micoli.php.peerNavigation.configuration

import io.swagger.v3.oas.annotations.media.Schema

class Associate {
    @Schema(
        description = "Regex pattern with named groups matching first class FQN",
        example = "\\\\App\\\\Tests\\\\Func\\\\(?<type>.*)\\\\Web\\\\(?<path>.*)\\\\ControllerTest")
    var classA: String? = null

    @Schema(
        description =
            "Pattern for second class FQN using `(?<groupName>.+)` substitution from named groups",
        example = "\\\\App\\\\(?<type>.*)\\\\Web\\\\(?<path>.*)\\\\Controller")
    var classB: String? = null
}
