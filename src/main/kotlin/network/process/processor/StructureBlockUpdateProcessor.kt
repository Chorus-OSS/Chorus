package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.blockentity.BlockEntityStructBlock
import org.chorus_oss.chorus.experimental.network.MigrationPacket
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.network.ProtocolInfo
import org.chorus_oss.chorus.network.process.DataPacketProcessor
import org.chorus_oss.protocol.packets.StructureBlockUpdatePacket

class StructureBlockUpdateProcessor : DataPacketProcessor<MigrationPacket<StructureBlockUpdatePacket>>() {
    override fun handle(player: Player, pk: MigrationPacket<StructureBlockUpdatePacket>) {
        val packet = pk.packet

        if (player.player.isOp && player.player.isCreative) {
            val blockEntity = player.player.level!!.getBlockEntity(Vector3(packet.position))
            if (blockEntity is BlockEntityStructBlock) {
                val sBlock = blockEntity.levelBlock
                sBlock.setPropertyValue(CommonBlockProperties.STRUCTURE_BLOCK_TYPE, packet.structureBlockType)
                blockEntity.updateSetting(packet)
                player.player.level!!.setBlock(blockEntity.position, sBlock, true)
                blockEntity.spawnTo(player.player)
            }
        }
    }

    override val packetId: Int = StructureBlockUpdatePacket.id
}
