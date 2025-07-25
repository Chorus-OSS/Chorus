package org.chorus_oss.chorus.level.particle

import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.utils.BlockColor

class DustParticle @JvmOverloads constructor(pos: Vector3, r: Int, g: Int, b: Int, a: Int = 255) : GenericParticle(
    pos,
    TYPE_FALLING_DUST,
    ((a and 0xff) shl 24) or ((r and 0xff) shl 16) or ((g and 0xff) shl 8) or (b and 0xff)
) {
    constructor(pos: Vector3, blockColor: BlockColor) : this(
        pos,
        blockColor.red,
        blockColor.green,
        blockColor.blue,
        blockColor.alpha
    )
}
