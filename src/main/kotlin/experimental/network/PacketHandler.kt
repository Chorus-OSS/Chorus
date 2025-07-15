package org.chorus_oss.chorus.experimental.network

import org.chorus_oss.protocol.core.Packet

interface PacketHandler {
    fun handle(packet: Packet)
}