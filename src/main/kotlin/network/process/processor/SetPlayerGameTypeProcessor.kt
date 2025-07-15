package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.command.Command
import org.chorus_oss.chorus.experimental.network.MigrationPacket
import org.chorus_oss.chorus.lang.TranslationContainer
import org.chorus_oss.chorus.network.process.DataPacketProcessor
import org.chorus_oss.protocol.packets.SetPlayerGameTypePacket
import org.chorus_oss.protocol.types.GameType

class SetPlayerGameTypeProcessor : DataPacketProcessor<MigrationPacket<SetPlayerGameTypePacket>>() {
    override fun handle(player: Player, pk: MigrationPacket<SetPlayerGameTypePacket>) {
        val packet = pk.packet

        if (packet.gameType.ordinal != player.gamemode && player.player.hasPermission("chorus.command.gamemode")) {
            player.player.setGamemode(
                when (packet.gameType) {
                    GameType.Survival, GameType.Creative, GameType.Adventure -> packet.gameType.ordinal
                    GameType.Spectator -> 3
                    GameType.Default -> Server.instance.defaultGamemode
                    else -> throw IllegalStateException("Unexpected value: " + packet.gameType)
                }
            )
            Command.broadcastCommandMessage(
                player.player,
                TranslationContainer(
                    "commands.gamemode.success.self",
                    Server.getGamemodeString(player.player.gamemode)
                )
            )
        }
    }

    override val packetId: Int = SetPlayerGameTypePacket.id
}
