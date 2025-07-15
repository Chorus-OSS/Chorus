package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.item.ItemFood
import org.chorus_oss.chorus.network.ProtocolInfo
import org.chorus_oss.chorus.network.process.DataPacketProcessor
import org.chorus_oss.chorus.network.protocol.EntityEventPacket

class EntityEventProcessor : DataPacketProcessor<EntityEventPacket>() {
    override fun handle(player: Player, pk: EntityEventPacket) {
        val player = player.player
        if (!player.spawned || !player.isAlive()) {
            return
        }

        if (pk.event == EntityEventPacket.EATING_ITEM) {
            if (pk.data == 0 || pk.eid != player.getRuntimeID()) {
                return
            }

            val hand = player.inventory.itemInHand as? ItemFood ?: return

            val predictedData = (hand.runtimeId shl 16) or hand.damage
            if (pk.data != predictedData) {
                return
            }

            pk.eid = player.getRuntimeID()
            pk.data = predictedData

            player.dataPacket(pk)
            Server.broadcastPacket(player.viewers.values, pk)
        } else if (pk.event == EntityEventPacket.ENCHANT) {
            if (pk.eid != player.getRuntimeID()) {
                return
            }
        }
    }

    override val packetId: Int
        get() = ProtocolInfo.ENTITY_EVENT_PACKET
}
