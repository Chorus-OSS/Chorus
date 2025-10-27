package org.chorus_oss.chorus.network.connection

object BedrockDisconnectReasons {
    const val DISCONNECTED: String = "disconnect.disconnected"
    const val CLOSED: String = "disconnect.closed"
    const val REMOVED: String = "disconnect.removed"
    const val TIMEOUT: String = "disconnect.timeout"
    const val UNKNOWN: String = "disconnect.lost"
}
