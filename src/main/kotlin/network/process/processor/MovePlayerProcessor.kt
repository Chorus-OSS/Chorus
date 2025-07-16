package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.experimental.network.MigrationPacket
import org.chorus_oss.chorus.level.Transform.Companion.fromObject
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.network.process.DataPacketProcessor
import org.chorus_oss.protocol.packets.MovePlayerPacket

class MovePlayerProcessor : DataPacketProcessor<MigrationPacket<MovePlayerPacket>>() {
    override fun handle(player: Player, pk: MigrationPacket<MovePlayerPacket>) {
        val packet = pk.packet

        val player = player.player
        if (Server.instance.getServerAuthoritativeMovement() > 0) {
            return
        }
        val newPos = Vector3(
            packet.position.x.toDouble(),
            (packet.position.y - player.player.getBaseOffset()).toDouble(),
            packet.position.z.toDouble()
        )

        var yaw = packet.yaw % 360f
        var headYaw = packet.headYaw % 360f
        val pitch = packet.pitch % 360f
        if (yaw < 0) {
            yaw += 360f
        }
        if (headYaw < 0) {
            headYaw += 360f
        }
        player.player.offerMovementTask(
            fromObject(
                newPos,
                player.level!!, yaw.toDouble(), pitch.toDouble(), headYaw.toDouble()
            )
        )
    }

    override val packetId: Int = MovePlayerPacket.id
}
