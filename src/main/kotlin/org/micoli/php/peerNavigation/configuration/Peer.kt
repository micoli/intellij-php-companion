package org.micoli.php.peerNavigation.configuration

import io.swagger.v3.oas.annotations.media.Schema

class Peer {
    @Schema(description = "Regex pattern with named groups matching source class FQN")
    var source: String? = null

    @Schema(
        description =
            "Target class FQN pattern using `(?<groupName>.+)` substitution from named groups")
    var target: String? = null
}
