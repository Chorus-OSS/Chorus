package org.chorus_oss.chorus.network.process.handler

import org.chorus_oss.chorus.network.connection.BedrockSession
import org.chorus_oss.chorus.network.process.SessionState
import org.chorus_oss.protocol.core.Packet
import org.chorus_oss.protocol.packets.ClientToServerHandshakePacket

class HandshakePacketHandler(session: BedrockSession) : BedrockSessionPacketHandler(session) {
    override fun handle(packet: Packet) {
        if (packet !is ClientToServerHandshakePacket) return

        session.machine.fire(SessionState.ResourcePack)
    }
}
