package org.chorus_oss.chorus.level.particle

import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.protocol.packets.LevelEventPacket
import org.chorus_oss.protocol.core.Packet
import org.chorus_oss.protocol.types.Vector3f

class BlockForceFieldParticle @JvmOverloads constructor(pos: Vector3, scale: Int = 0) :
    GenericParticle(pos, TYPE_BLOCK_FORCE_FIELD) {
    override fun encode(): List<Packet> {
        val pk = LevelEventPacket(
            eventType = LevelEventPacket.PARTICLE_DENY_BLOCK,
            position = Vector3f(
                x = x.toFloat(),
                y = y.toFloat(),
                z = z.toFloat()
            ),
            eventData = data
        )
        return listOf(pk)
    }
}
