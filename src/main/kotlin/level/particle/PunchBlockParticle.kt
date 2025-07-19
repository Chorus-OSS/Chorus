package org.chorus_oss.chorus.level.particle

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.protocol.packets.LevelEventPacket
import org.chorus_oss.protocol.core.Packet
import org.chorus_oss.protocol.types.Vector3f

class PunchBlockParticle(pos: Vector3, block: Block) :
    Particle(pos.x, pos.y, pos.z) {
    protected val data: Int

    init {
        this.data = block.blockState.blockStateHash()
    }

    override fun encode(): List<Packet> {
        val pk = LevelEventPacket(
            eventType = LevelEventPacket.PARTICLE_CRACK_BLOCK,
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
