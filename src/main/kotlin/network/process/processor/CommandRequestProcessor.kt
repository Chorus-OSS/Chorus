package org.chorus_oss.chorus.network.process.processor

import com.google.common.util.concurrent.RateLimiter
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.event.player.PlayerCommandPreprocessEvent
import org.chorus_oss.chorus.event.player.PlayerHackDetectedEvent
import org.chorus_oss.chorus.experimental.network.MigrationPacket
import org.chorus_oss.chorus.network.process.DataPacketProcessor
import org.chorus_oss.protocol.packets.CommandRequestPacket
import java.util.concurrent.TimeUnit

class CommandRequestProcessor :
    DataPacketProcessor<MigrationPacket<CommandRequestPacket>>() {
    val rateLimiter: RateLimiter = RateLimiter.create(500.0)

    override fun handle(player: Player, pk: MigrationPacket<CommandRequestPacket>) {
        val packet = pk.packet

        val length = packet.command.length
        if (!rateLimiter.tryAcquire(length, 300, TimeUnit.MILLISECONDS)) {
            val event = PlayerHackDetectedEvent(player.player, PlayerHackDetectedEvent.HackType.COMMAND_SPAM)
            Server.instance.pluginManager.callEvent(event)

            if (event.isKick) player.player.session.close("kick because hack")
            return
        }
        if (!player.player.spawned || !player.player.isAlive()) {
            return
        }
        val playerCommandPreprocessEvent = PlayerCommandPreprocessEvent(player.player, packet.command)
        Server.instance.pluginManager.callEvent(playerCommandPreprocessEvent)
        if (playerCommandPreprocessEvent.cancelled) {
            return
        }
        Server.instance.executeCommand(playerCommandPreprocessEvent.player, playerCommandPreprocessEvent.message!!)
    }

    override val packetId: Int = CommandRequestPacket.id
}
