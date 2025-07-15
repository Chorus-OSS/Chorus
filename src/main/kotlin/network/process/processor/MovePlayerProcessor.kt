package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.level.Transform.Companion.fromObject
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.network.ProtocolInfo
import org.chorus_oss.chorus.network.process.DataPacketProcessor
import org.chorus_oss.chorus.network.protocol.MovePlayerPacket

class MovePlayerProcessor : DataPacketProcessor<MovePlayerPacket>() {
    override fun handle(player: Player, pk: MovePlayerPacket) {
        val player = player.player
        if (Server.instance.getServerAuthoritativeMovement() > 0) {
            return
        }
        val newPos = Vector3(pk.x.toDouble(), (pk.y - player.player.getBaseOffset()).toDouble(), pk.z.toDouble())

        pk.yaw %= 360f
        pk.headYaw %= 360f
        pk.pitch %= 360f
        if (pk.yaw < 0) {
            pk.yaw += 360f
        }
        if (pk.headYaw < 0) {
            pk.headYaw += 360f
        }
        player.player.offerMovementTask(
            fromObject(
                newPos,
                player.level!!, pk.yaw.toDouble(), pk.pitch.toDouble(), pk.headYaw.toDouble()
            )
        )
    }

    override val packetId: Int
        get() = ProtocolInfo.MOVE_PLAYER_PACKET
}
