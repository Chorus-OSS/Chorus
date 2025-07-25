package org.chorus_oss.chorus.network.connection

import org.cloudburstmc.netty.channel.raknet.RakDisconnectReason
import java.util.*

object BedrockDisconnectReasons {
    const val DISCONNECTED: String = "disconnect.disconnected"
    const val CLOSED: String = "disconnect.closed"
    const val REMOVED: String = "disconnect.removed"
    const val TIMEOUT: String = "disconnect.timeout"
    const val UNKNOWN: String = "disconnect.lost"

    private val FROM_RAKNET = generateRakNetMappings()

    private fun generateRakNetMappings(): Map<RakDisconnectReason, String> {
        val map = EnumMap<RakDisconnectReason, String>(
            RakDisconnectReason::class.java
        )
        map[RakDisconnectReason.CLOSED_BY_REMOTE_PEER] = CLOSED
        map[RakDisconnectReason.DISCONNECTED] = DISCONNECTED
        map[RakDisconnectReason.TIMED_OUT] = TIMEOUT
        map[RakDisconnectReason.BAD_PACKET] = REMOVED

        return Collections.unmodifiableMap(map)
    }

    fun getReason(reason: RakDisconnectReason): String {
        return FROM_RAKNET.getOrDefault(reason, reason.name)
    }
}
