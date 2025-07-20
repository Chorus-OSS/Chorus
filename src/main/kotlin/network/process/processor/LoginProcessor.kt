package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.event.player.PlayerDuplicatedLoginEvent
import org.chorus_oss.chorus.network.process.PacketProcessor
import org.chorus_oss.protocol.packets.LoginPacket

class LoginProcessor : PacketProcessor<LoginPacket> {
    override fun handle(player: Player, packet: LoginPacket) {

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

    override val packetID: Int = LoginPacket.id
}
