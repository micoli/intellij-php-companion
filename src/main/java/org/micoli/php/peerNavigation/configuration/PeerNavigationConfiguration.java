package org.micoli.php.peerNavigation.configuration;

import io.swagger.v3.oas.annotations.media.Schema;

public final class PeerNavigationConfiguration {
    @Schema(description = "Array of one-way navigation rules")
    public Peer[] peers = new Peer[] {};

    @Schema(description = "Array of bidirectional navigation rules")
    public Associate[] associates = new Associate[] {};
}
