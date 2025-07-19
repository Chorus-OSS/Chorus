package org.chorus_oss.chorus.level.particle

import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.protocol.packets.LevelEventPacket
import org.chorus_oss.protocol.core.Packet
import org.chorus_oss.protocol.types.Vector3f

class BoneMealParticle(pos: Vector3) : Particle(pos.x, pos.y, pos.z) {
    override fun encode(): List<Packet> {
        val pk = LevelEventPacket(
            eventType = LevelEventPacket.PARTICLE_CROP_GROWTH,
            position = Vector3f(
                x = x.toFloat(),
                y = y.toFloat(),
                z = z.toFloat()
            ),
            eventData = 0
        )
        return listOf(pk)
    }
}
