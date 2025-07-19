package org.chorus_oss.chorus.level.particle

import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.protocol.packets.LevelEventPacket
import org.chorus_oss.protocol.core.Packet
import org.chorus_oss.protocol.types.Vector3f

open class GenericParticle @JvmOverloads constructor(pos: Vector3, id: Int, data: Int = 0) :
    Particle(pos.x, pos.y, pos.z) {
    protected val data: Int
    protected var id: Int = 0

    init {
        this.id = id
        this.data = data
    }

    override fun encode(): List<Packet> {
        val pk = LevelEventPacket(
            eventType = (LevelEventPacket.ADD_PARTICLE_MASK or this.id),
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
