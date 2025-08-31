package org.micoli.php.peerNavigation.configuration;

import io.swagger.v3.oas.annotations.media.Schema;

public final class Associate {
    @Schema(
            description = "Regex pattern with named groups matching first class FQN",
            example = "\\\\App\\\\Tests\\\\Func\\\\(?<type>.*)\\\\Web\\\\(?<path>.*)\\\\ControllerTest")
    public String classA;

    @Schema(
            description = "Pattern for second class FQN using `(?<groupName>.+)` substitution from named groups",
            example = "\\\\App\\\\(?<type>.*)\\\\Web\\\\(?<path>.*)\\\\Controller")
    public String classB;
}
