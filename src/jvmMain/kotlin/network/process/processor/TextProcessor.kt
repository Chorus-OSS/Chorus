package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.network.process.PacketProcessor
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.protocol.packets.TextPacket

class TextProcessor : PacketProcessor<TextPacket> {
    override fun handle(player: Player, packet: TextPacket) {
        if (!player.player.spawned || !player.player.isAlive()) {
            return
        }

        val isXboxAuth = Server.instance.settings.serverSettings.xboxAuth
        if (isXboxAuth && packet.xuid != player.player.loginChainData.xuid) {
            log.warn(
                "{} sent TextPacket with invalid xuid : {} != {}",
                player.player.getEntityName(),
                packet.xuid,
                player.player.loginChainData.xuid
            )
            return
        }

        if (!packet.parameters.isNullOrEmpty()) {
            player.player.close("§cPacket handling error")
            return
        }

        if (packet.textType == TextPacket.Companion.TextType.Chat) {
            var chatMessage = packet.message
            val breakLine = chatMessage.indexOf('\n')
            // Chat messages shouldn't contain break lines so ignore text afterward
            if (breakLine != -1) {
                chatMessage = chatMessage.substring(0, breakLine)
            }
            player.player.chat(chatMessage)
        }
    }

    override val packetID: Int = TextPacket.id

    companion object : Loggable
}
