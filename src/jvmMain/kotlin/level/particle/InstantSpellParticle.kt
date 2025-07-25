package org.chorus_oss.chorus.level.particle

import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.utils.BlockColor

class InstantSpellParticle : SpellParticle {
    override var data: Int = 0

    @JvmOverloads
    constructor(pos: Vector3, data: Int = 0) : super(pos, data)

    constructor(pos: Vector3, blockColor: BlockColor) : this(pos, blockColor.red, blockColor.green, blockColor.blue)

    constructor(pos: Vector3, r: Int, g: Int, b: Int) : super(pos, r, g, b, 0x01)
}
