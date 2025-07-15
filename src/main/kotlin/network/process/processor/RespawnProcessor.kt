package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.experimental.network.MigrationPacket
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.network.process.DataPacketProcessor
import org.chorus_oss.protocol.packets.RespawnPacket
import org.chorus_oss.protocol.types.Vector3f

class RespawnProcessor : DataPacketProcessor<MigrationPacket<RespawnPacket>>() {
    override fun handle(player: Player, pk: MigrationPacket<RespawnPacket>) {
        val packet = pk.packet

        val player = player.player
        if (player.isAlive()) {
            return
        }
        if (packet.state == RespawnPacket.Companion.State.ReadyToSpawn) {
            val respawn1 = RespawnPacket(
                position = Vector3f(player.position),
                state = RespawnPacket.Companion.State.ReadyToSpawn,
                entityRuntimeID = player.getRuntimeID().toULong(),
            )
            player.sendPacket(respawn1)
        }
    }

    override val packetId: Int = RespawnPacket.id
}
