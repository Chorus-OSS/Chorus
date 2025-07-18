package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.event.player.PlayerDuplicatedLoginEvent
import org.chorus_oss.chorus.experimental.network.MigrationPacket
import org.chorus_oss.chorus.network.ProtocolInfo
import org.chorus_oss.chorus.network.process.DataPacketProcessor
import org.chorus_oss.protocol.packets.LoginPacket

class LoginProcessor : DataPacketProcessor<MigrationPacket<LoginPacket>>() {
    override fun handle(player: Player, pk: MigrationPacket<LoginPacket>) {
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

    override val packetId: Int = LoginPacket.id
}
