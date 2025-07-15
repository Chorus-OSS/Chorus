package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.network.ProtocolInfo
import org.chorus_oss.chorus.network.process.DataPacketProcessor
import org.chorus_oss.chorus.network.protocol.LevelSoundEventPacket

open class LevelSoundEventProcessor : DataPacketProcessor<LevelSoundEventPacket>() {
    override fun handle(player: Player, pk: LevelSoundEventPacket) {
        val player = player.player
        if (!player.isSpectator) {
            player.level!!.addChunkPacket(player.position.chunkX, player.position.chunkZ, pk)
        }
    }

    override val packetId: Int
        get() = ProtocolInfo.LEVEL_SOUND_EVENT_PACKET
}
