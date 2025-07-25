package org.chorus_oss.chorus.network.process

import org.chorus_oss.chorus.Player
import org.chorus_oss.protocol.core.Packet

interface PacketProcessor<T : Packet> {
    fun handle(player: Player, packet: T)

    val packetID: Int
}
