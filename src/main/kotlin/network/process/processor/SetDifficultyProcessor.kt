package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.command.Command
import org.chorus_oss.chorus.lang.TranslationContainer
import org.chorus_oss.chorus.network.process.PacketProcessor
import org.chorus_oss.protocol.packets.SetDifficultyPacket

class SetDifficultyProcessor : PacketProcessor<SetDifficultyPacket> {
    override fun handle(player: Player, packet: SetDifficultyPacket) {
        if (!player.player.spawned || !player.player.hasPermission("chorus.command.difficulty")) {
            return
        }
        Server.instance.setDifficulty(packet.difficulty.toInt())
        val difficultyPacket = SetDifficultyPacket(
            difficulty = Server.instance.getDifficulty().toUInt()
        )
        Server.broadcastPacket(Server.instance.onlinePlayers.values, difficultyPacket)
        Command.broadcastCommandMessage(
            player.player,
            TranslationContainer(
                "commands.difficulty.success",
                Server.instance.getDifficulty().toString()
            )
        )
    }

    override val packetID: Int = SetDifficultyPacket.id
}
