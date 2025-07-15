package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.entity.item.EntityBoat
import org.chorus_oss.chorus.event.vehicle.VehicleMoveEvent
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.network.ProtocolInfo
import org.chorus_oss.chorus.network.process.DataPacketProcessor
import org.chorus_oss.chorus.network.protocol.MoveEntityAbsolutePacket

class MoveEntityAbsoluteProcessor : DataPacketProcessor<MoveEntityAbsolutePacket>() {
    override fun handle(player: Player, pk: MoveEntityAbsolutePacket) {
        val player = player.player
        if (!player.isAlive() || !player.spawned || player.getRiding() == null) {
            return
        }
        val movedEntity = player.level!!.getEntity(pk.eid) as? EntityBoat ?: return

        val pos = Vector3(pk.x, pk.y - movedEntity.getBaseOffset(), pk.z)
        if (!movedEntity.equals(player.getRiding()) || !movedEntity.isControlling(player) || pos.distanceSquared(
                movedEntity.position
            ) > 10 * 10
        ) {
            movedEntity.moveDelta()
            return
        }

        val from = movedEntity.transform
        movedEntity.setPositionAndRotation(pos, pk.headYaw, 0.0)
        val to = movedEntity.transform
        if (from != to) {
            Server.instance.pluginManager.callEvent(VehicleMoveEvent(player, from, to))
        }
    }

    override val packetId: Int
        get() = ProtocolInfo.MOVE_ENTITY_ABSOLUTE_PACKET
}
