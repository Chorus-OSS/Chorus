package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.network.process.PacketProcessor
import org.chorus_oss.protocol.packets.SettingsCommandPacket

class SettingsCommandProcessor : PacketProcessor<SettingsCommandPacket> {
    override fun handle(player: Player, packet: SettingsCommandPacket) {
        val command = packet.commandLine.lowercase()
        Server.instance.executeCommand(player, command)
    }

    override val packetID: Int = SettingsCommandPacket.id
}
