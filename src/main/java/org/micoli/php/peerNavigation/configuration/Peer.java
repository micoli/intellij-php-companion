package org.micoli.php.peerNavigation.configuration;

import io.swagger.v3.oas.annotations.media.Schema;

public final class Peer {
    @Schema(description = "Regex pattern with named groups matching source class FQN", example = "")
    public String source;

    @Schema(description = "Target class FQN pattern using `(?<groupName>.+)` substitution from named groups")
    public String target;
}
