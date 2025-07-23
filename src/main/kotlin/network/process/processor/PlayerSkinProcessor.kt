package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.entity.data.Skin
import org.chorus_oss.chorus.event.player.PlayerChangeSkinEvent
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.network.process.PacketProcessor
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.protocol.packets.PlayerSkinPacket
import java.util.concurrent.TimeUnit

class PlayerSkinProcessor : PacketProcessor<PlayerSkinPacket> {
    override fun handle(player: Player, packet: PlayerSkinPacket) {

        val skin = Skin(packet.skin)

        if (!skin.isValid()) {
            log.warn(player.player.getEntityName() + ": PlayerSkinPacket with invalid skin")
            return
        }

        if (Server.instance.settings.playerSettings.forceSkinTrusted) {
            skin.setTrusted(true)
        }

        val playerChangeSkinEvent = PlayerChangeSkinEvent(player, skin)
        val tooQuick = TimeUnit.SECONDS.toMillis(
            Server.instance.settings.playerSettings.skinChangeCooldown.toLong()
        ) > System.currentTimeMillis() - player.lastSkinChange
        if (tooQuick) {
            playerChangeSkinEvent.cancelled = true
            log.warn("Player " + player.player.getEntityName() + " change skin too quick!")
        }
        Server.instance.pluginManager.callEvent(playerChangeSkinEvent)
        if (!playerChangeSkinEvent.cancelled) {
            player.lastSkinChange = System.currentTimeMillis()
            player.skin = (skin)
        }
    }

    override val packetID: Int = PlayerSkinPacket.id

    companion object : Loggable
}
