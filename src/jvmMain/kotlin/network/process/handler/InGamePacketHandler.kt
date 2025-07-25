package org.chorus_oss.chorus.network.process.handler

import org.chorus_oss.chorus.network.connection.BedrockSession
import org.chorus_oss.chorus.network.process.PacketManager
import org.chorus_oss.protocol.core.Packet

class InGamePacketHandler(session: BedrockSession) : BedrockSessionPacketHandler(session) {
    val manager: PacketManager = PacketManager()

    fun managerHandle(packet: Packet) {
        if (manager.canProcess(packet.id)) {
            manager.processPacket(player!!, packet)
        }
    }
}
