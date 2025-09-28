package org.micoli.php.peerNavigation.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.v3.oas.annotations.media.Schema

class Associate {
    @Schema(
        description = "Regex pattern with named groups matching first class FQN",
        example = "\\\\App\\\\Tests\\\\Func\\\\(?<type>.*)\\\\Web\\\\(?<path>.*)\\\\ControllerTest")
    lateinit var classA: String

    @Schema(
        description =
            "Pattern for second class FQN using `(?<groupName>.+)` substitution from named groups",
        example = "\\\\App\\\\(?<type>.*)\\\\Web\\\\(?<path>.*)\\\\Controller")
    lateinit var classB: String

    @JsonIgnore
    fun isFullyInitialized(): Boolean {
        return ::classA.isInitialized && ::classB.isInitialized
    }
}
