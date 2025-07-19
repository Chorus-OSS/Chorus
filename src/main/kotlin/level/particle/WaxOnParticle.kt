package org.chorus_oss.chorus.level.particle

import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.protocol.packets.LevelEventPacket
import org.chorus_oss.protocol.core.Packet
import org.chorus_oss.protocol.types.Vector3f

class WaxOnParticle(pos: Vector3) : GenericParticle(pos, TYPE_WAX) {
    override fun encode(): List<Packet> {
        val pk = LevelEventPacket(
            eventType = LevelEventPacket.PARTICLE_WAX_ON,
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
