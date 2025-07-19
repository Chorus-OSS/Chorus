package org.chorus_oss.chorus.level.particle

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.protocol.packets.LevelEventPacket
import org.chorus_oss.protocol.core.Packet
import org.chorus_oss.protocol.types.Vector3f

class DestroyBlockParticle(pos: Vector3, block: Block) : Particle(pos.x, pos.y, pos.z) {
    private val data: Int

    init {
        this.data = block.runtimeId
    }

    override fun encode(): List<Packet> {
        val pk = LevelEventPacket(
            eventType = LevelEventPacket.PARTICLE_DESTROY_BLOCK,
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
