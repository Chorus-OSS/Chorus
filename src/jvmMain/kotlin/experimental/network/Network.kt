package org.chorus_oss.chorus.experimental.network

import io.ktor.network.sockets.*
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.experimental.network.connection.BedrockMessage
import org.chorus_oss.chorus.experimental.network.connection.BedrockPeer
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.chorus.utils.Utils
import org.chorus_oss.protocol.ProtocolInfo
import org.chorus_oss.raknet.rakServer
import org.chorus_oss.raknet.server.RakServer

class Network(val server: Server) {
    val message: BedrockMessage
        get() = BedrockMessage(
            name = server.motd,
            subName = server.subMotd,
            playerCount = server.onlinePlayers.size,
            playerMax = server.maxPlayers,
            guid = server.serverID.mostSignificantBits.toULong(),
            gamemode = Server.getGamemodeString(server.defaultGamemode, true),
            protocol = ProtocolInfo.VERSION,
            port = server.port,
        )

    private val peers: MutableMap<InetSocketAddress, BedrockPeer> = mutableMapOf()

    private val rak: RakServer = rakServer(server.ip, server.port) {
        security = true
        packetLimit = server.settings.networkSettings.packetLimit
        message = this@Network.message.toByteString()

        onConnect { session ->
            peers[session.address] = BedrockPeer(session)
        }

        onDisconnect { session ->
            peers.remove(session.address)?.close()
        }
    }.start()

    fun updateMessage() {
        rak.config.message = message.toByteString()
    }

    fun shutdown() {
        rak.stop()
    }

    fun tick() {
        try {
            for (peer in peers.values) {
                peer.tick()
            }
        } catch (e: Exception) {
            log.error(
                server.lang.tr(
                    "chorus.server.networkError",
                    javaClass.name, Utils.getExceptionMessage(e)
                ), e
            )
        }
    }

    fun onSessionDisconnect(address: InetSocketAddress) {
        peers.remove(address)?.close()
    }

    companion object : Loggable
}