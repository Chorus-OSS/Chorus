package org.chorus_oss.chorus.level.particle

import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.utils.BlockColor
import org.chorus_oss.protocol.core.Packet
import org.chorus_oss.protocol.packets.LevelEventPacket
import org.chorus_oss.protocol.types.Vector3f

open class SpellParticle @JvmOverloads constructor(pos: Vector3, protected open val data: Int = 0) :
    Particle(pos.x, pos.y, pos.z) {
    constructor(pos: Vector3, blockColor: BlockColor) : this(pos, blockColor.red, blockColor.green, blockColor.blue)

    constructor(pos: Vector3, r: Int, g: Int, b: Int) : this(pos, r, g, b, 0x00)

    protected constructor(pos: Vector3, r: Int, g: Int, b: Int, a: Int) : this(
        pos,
        ((a and 0xff) shl 24) or ((r and 0xff) shl 16) or ((g and 0xff) shl 8) or (b and 0xff)
    )

    override fun encode(): List<Packet> {
        val pk = LevelEventPacket(
            eventType = LevelEventPacket.PARTICLE_POTION_SPLASH,
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
