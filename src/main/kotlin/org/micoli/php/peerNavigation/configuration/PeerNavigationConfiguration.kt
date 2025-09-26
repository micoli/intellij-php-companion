package org.micoli.php.peerNavigation.configuration

import io.swagger.v3.oas.annotations.media.Schema

class PeerNavigationConfiguration {
    @Schema(description = "Array of one-way navigation rules") var peers: Array<Peer> = arrayOf()

    @Schema(description = "Array of bidirectional navigation rules")
    var associates: Array<Associate> = arrayOf()
}
