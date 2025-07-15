package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.experimental.network.MigrationPacket
import org.chorus_oss.chorus.inventory.SpecialWindowId
import org.chorus_oss.chorus.network.process.DataPacketProcessor
import org.chorus_oss.protocol.packets.PlayerHotbarPacket

class PlayerHotbarProcessor : DataPacketProcessor<MigrationPacket<PlayerHotbarPacket>>() {
    override fun handle(player: Player, pk: MigrationPacket<PlayerHotbarPacket>) {
        val packet = pk.packet

        if (packet.windowID.toInt() != SpecialWindowId.PLAYER.id) {
            return  //In PE this should never happen
        }
        player.player.inventory.equipItem(packet.selectedHotbarSlot.toInt())
    }

    override val packetId: Int = PlayerHotbarPacket.id
}
