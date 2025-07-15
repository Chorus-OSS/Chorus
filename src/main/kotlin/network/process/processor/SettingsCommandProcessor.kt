package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.experimental.network.MigrationPacket
import org.chorus_oss.chorus.network.process.DataPacketProcessor
import org.chorus_oss.protocol.packets.SettingsCommandPacket

class SettingsCommandProcessor : DataPacketProcessor<MigrationPacket<SettingsCommandPacket>>() {
    override fun handle(player: Player, pk: MigrationPacket<SettingsCommandPacket>) {
        val packet = pk.packet

        val player = player.player.asPlayer()

        val command = packet.commandLine.lowercase()
        Server.instance.executeCommand(player, command)
    }

    override val packetId: Int = SettingsCommandPacket.id
}
