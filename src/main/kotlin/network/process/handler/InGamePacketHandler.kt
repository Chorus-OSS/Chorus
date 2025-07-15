package org.chorus_oss.chorus.network.process.handler

import org.chorus_oss.chorus.network.DataPacket
import org.chorus_oss.chorus.network.connection.BedrockSession
import org.chorus_oss.chorus.network.process.DataPacketManager

class InGamePacketHandler(session: BedrockSession) : BedrockSessionPacketHandler(session) {
    val manager: DataPacketManager = DataPacketManager()

    fun managerHandle(pk: DataPacket) {
        if (manager.canProcess(pk.pid())) {
            manager.processPacket(player!!, pk)
        }
    }
}
