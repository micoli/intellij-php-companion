package org.micoli.php.peerNavigation.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.v3.oas.annotations.media.Schema

class Peer {
    @Schema(description = "Regex pattern with named groups matching source class FQN")
    lateinit var source: String

    @Schema(
        description =
            "Target class FQN pattern using `(?<groupName>.+)` substitution from named groups")
    lateinit var target: String

    @JsonIgnore
    fun isFullyInitialized(): Boolean {
        return ::source.isInitialized && ::target.isInitialized
    }
}
