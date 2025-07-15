package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.event.player.PlayerDuplicatedLoginEvent
import org.chorus_oss.chorus.network.ProtocolInfo
import org.chorus_oss.chorus.network.process.DataPacketProcessor
import org.chorus_oss.chorus.network.protocol.LoginPacket

class LoginProcessor : DataPacketProcessor<LoginPacket>() {
    override fun handle(player: Player, pk: LoginPacket) {
        val player = player.player
        if (!player.session.authenticated) {
            return
        }

        val event = PlayerDuplicatedLoginEvent(player)
        Server.instance.pluginManager.callEvent(event)

        if (event.cancelled) {
            return
        }

        player.close("§cPacket handling error")
    }

    override val packetId: Int
        get() = ProtocolInfo.LOGIN_PACKET
}
