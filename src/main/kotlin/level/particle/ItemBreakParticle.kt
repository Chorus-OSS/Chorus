package org.chorus_oss.chorus.level.particle

import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.network.DataPacket
import org.chorus_oss.chorus.network.protocol.LevelEventPacket

class ItemBreakParticle(pos: Vector3, item: Item) :
    Particle(pos.x, pos.y, pos.z) {
    private val data: Int

    init {
        this.data = (item.runtimeId shl 16 or item.damage)
    }

    override fun encode(): Array<DataPacket> {
        val packet = LevelEventPacket()
        packet.evid = (LevelEventPacket.EVENT_ADD_PARTICLE_MASK or TYPE_ICON_CRACK).toShort().toInt()
        packet.x = x.toFloat()
        packet.y = y.toFloat()
        packet.z = z.toFloat()
        packet.data = this.data
        return arrayOf(packet)
    }
}
