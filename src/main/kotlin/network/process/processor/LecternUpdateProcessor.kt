package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.BlockLectern
import org.chorus_oss.chorus.blockentity.BlockEntityLectern
import org.chorus_oss.chorus.event.block.LecternPageChangeEvent
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.network.process.PacketProcessor
import org.chorus_oss.protocol.packets.LecternUpdatePacket

class LecternUpdateProcessor : PacketProcessor<LecternUpdatePacket> {
    override fun handle(player: Player, packet: LecternUpdatePacket) {
        val blockPosition = Vector3(packet.blockPosition)
        val blockEntityLectern = player.player.level!!.getBlockEntity(blockPosition)
        if (blockEntityLectern is BlockEntityLectern) {
            val lecternPageChangeEvent = LecternPageChangeEvent(player.player, blockEntityLectern, packet.page.toInt())
            Server.instance.pluginManager.callEvent(lecternPageChangeEvent)
            if (!lecternPageChangeEvent.cancelled) {
                blockEntityLectern.rawPage = lecternPageChangeEvent.newRawPage
                blockEntityLectern.spawnToAll()
                val blockLectern = blockEntityLectern.block
                if (blockLectern is BlockLectern) {
                    blockLectern.executeRedstonePulse()
                }
            }
        }
    }

    override val packetID: Int = LecternUpdatePacket.id
}
