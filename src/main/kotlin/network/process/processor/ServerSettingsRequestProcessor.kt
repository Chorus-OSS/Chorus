package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.event.player.PlayerServerSettingsRequestEvent
import org.chorus_oss.chorus.experimental.network.MigrationPacket
import org.chorus_oss.chorus.network.process.DataPacketProcessor
import org.chorus_oss.protocol.packets.ServerSettingsRequestPacket

class ServerSettingsRequestProcessor : DataPacketProcessor<MigrationPacket<ServerSettingsRequestPacket>>() {
    override fun handle(player: Player, pk: MigrationPacket<ServerSettingsRequestPacket>) {
        pk.packet

        val settingsRequestEvent =
            PlayerServerSettingsRequestEvent(player.player, HashMap(player.player.serverSettings))
        Server.instance.pluginManager.callEvent(settingsRequestEvent)

        if (!settingsRequestEvent.cancelled) {
            settingsRequestEvent.getSettings().forEach { (id, window) ->
                val re = org.chorus_oss.protocol.packets.ServerSettingsResponsePacket(
                    formID = id,
                    formData = window.toJson(),
                )
                player.sendPacket(re)
            }
        }
    }

    override val packetId: Int = ServerSettingsRequestPacket.id
}
