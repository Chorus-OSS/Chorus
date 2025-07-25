package org.chorus_oss.chorus.level.particle

import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.protocol.core.Packet
import org.chorus_oss.protocol.packets.LevelEventPacket
import org.chorus_oss.protocol.types.Vector3f

class ItemBreakParticle(pos: Vector3, item: Item) :
    Particle(pos.x, pos.y, pos.z) {
    private val data: Int

    init {
        this.data = (item.runtimeId shl 16 or item.damage)
    }

    override fun encode(): List<Packet> {
        val packet = LevelEventPacket(
            eventType = (LevelEventPacket.ADD_PARTICLE_MASK or TYPE_ICON_CRACK),
            position = Vector3f(
                x = x.toFloat(),
                y = y.toFloat(),
                z = z.toFloat(),
            ),
            eventData = data,
        )
        return listOf(packet)
    }
}
