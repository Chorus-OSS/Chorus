package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.event.player.PlayerChangeSkinEvent
import org.chorus_oss.chorus.network.ProtocolInfo
import org.chorus_oss.chorus.network.process.DataPacketProcessor
import org.chorus_oss.chorus.network.protocol.PlayerSkinPacket
import org.chorus_oss.chorus.utils.Loggable
import java.util.concurrent.TimeUnit

class PlayerSkinProcessor : DataPacketProcessor<PlayerSkinPacket>() {
    override fun handle(player: Player, pk: PlayerSkinPacket) {
        val player = player.player
        val skin = pk.skin

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

    override val packetId: Int
        get() = ProtocolInfo.PLAYER_SKIN_PACKET

    companion object : Loggable
}
