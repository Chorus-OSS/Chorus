package org.chorus_oss.chorus.level.particle

import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.network.DataPacket
import org.chorus_oss.chorus.network.protocol.LevelEventPacket
import org.chorus_oss.chorus.utils.BlockColor

open class SpellParticle @JvmOverloads constructor(pos: Vector3, protected open val data: Int = 0) :
    Particle(pos.x, pos.y, pos.z) {
    constructor(pos: Vector3, blockColor: BlockColor) : this(pos, blockColor.red, blockColor.green, blockColor.blue)

    constructor(pos: Vector3, r: Int, g: Int, b: Int) : this(pos, r, g, b, 0x00)

    protected constructor(pos: Vector3, r: Int, g: Int, b: Int, a: Int) : this(
        pos,
        ((a and 0xff) shl 24) or ((r and 0xff) shl 16) or ((g and 0xff) shl 8) or (b and 0xff)
    )

    override fun encode(): Array<DataPacket> {
        val pk = LevelEventPacket()
        pk.evid = LevelEventPacket.EVENT_PARTICLE_POTION_SPLASH
        pk.x = x.toFloat()
        pk.y = y.toFloat()
        pk.z = z.toFloat()
        pk.data = this.data

        return arrayOf(pk)
    }
}
