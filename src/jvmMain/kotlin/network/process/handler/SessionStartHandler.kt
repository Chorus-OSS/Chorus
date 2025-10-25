package org.chorus_oss.chorus.network.process.handler

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.network.connection.BedrockSession
import org.chorus_oss.chorus.network.process.SessionState
import org.chorus_oss.chorus.network.protocol.types.PacketCompressionAlgorithm
import org.chorus_oss.protocol.ProtocolInfo
import org.chorus_oss.protocol.core.Packet
import org.chorus_oss.protocol.packets.PlayStatusPacket
import org.chorus_oss.protocol.packets.RequestNetworkSettingsPacket

class SessionStartHandler(session: BedrockSession) : BedrockSessionPacketHandler(session) {
    override fun handle(packet: Packet) {
        if (packet !is RequestNetworkSettingsPacket) return

        val protocol = packet.clientProtocol
        if (protocol != ProtocolInfo.VERSION) {
            session.sendPlayStatus(
                if (protocol < ProtocolInfo.VERSION)
                    PlayStatusPacket.Companion.Status.LoginFailedClient
                else PlayStatusPacket.Companion.Status.LoginFailedServer,
                true
            )
            val message =
                if (protocol < ProtocolInfo.VERSION) "disconnectionScreen.outdatedClient" else "disconnectionScreen.outdatedServer"
            session.close(message)
            return
        }

        val server = Server.instance
        if (server.bannedIPs.isBanned(session.addressString)) {
            val reason = server.bannedIPs.entries[session.addressString]!!.reason
            session.close(if (reason.isNotEmpty()) "You are banned. Reason: $reason" else "You are banned")
            return
        }

        val settingsPacket = org.chorus_oss.protocol.packets.NetworkSettingsPacket(
            compressionThreshold = 1u, // compress everything
            compressionAlgorithm = when (Server.instance.settings.networkSettings.snappy) {
                true -> org.chorus_oss.protocol.types.PacketCompressionAlgorithm.Snappy
                false -> org.chorus_oss.protocol.types.PacketCompressionAlgorithm.Zlib
            },
            clientThrottle = false,
            clientThrottleThreshold = 0,
            clientThrottleScalar = 0f,
        )
        //In raknet version 11, the client does not enable packet compression by default,but the server will tell client what the
        //compression algorithm through NetworkSettingsPacket
        session.sendNetworkSettingsPacket(settingsPacket)

        val algorithm = when (Server.instance.settings.networkSettings.snappy) {
            true -> PacketCompressionAlgorithm.SNAPPY
            false -> PacketCompressionAlgorithm.ZLIB
        }

        session.setCompression(algorithm) //so send the NetworkSettingsPacket packet before set the session compression
        session.machine.fire(SessionState.Login)
    }
}
