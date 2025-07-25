package org.chorus_oss.chorus.level.particle

import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.protocol.core.Packet
import org.chorus_oss.protocol.types.Vector3f

class MobSpawnParticle(pos: Vector3, width: Float, height: Float) :
    Particle(pos.x, pos.y, pos.z) {
    private val width: Int = width.toInt()
    private val height: Int = height.toInt()

    override fun encode(): List<Packet> {
        val packet = org.chorus_oss.protocol.packets.LevelEventPacket(
            eventType = org.chorus_oss.protocol.packets.LevelEventPacket.PARTICLE_MOB_BLOCK_SPAWN,
            position = Vector3f(x.toFloat(), y.toFloat(), z.toFloat()),
            eventData = (this.width and 0xff) + ((this.height and 0xff) shl 8)
        )
        return listOf(packet)
    }
}
