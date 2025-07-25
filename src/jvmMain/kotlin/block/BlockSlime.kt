package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.math.BlockFace

class BlockSlime(blockState: BlockState = properties.defaultState) : BlockTransparent(blockState) {
    override val hardness: Double
        get() = 0.0

    override val name: String
        get() = "Slime Block"

    override val resistance: Double
        get() = 0.0

    override val lightFilter: Int
        get() = 1

    override fun canSticksBlock(): Boolean {
        return true
    }

    override val isSolid: Boolean
        get() = true

    override fun isSolid(side: BlockFace): Boolean {
        return true
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.SLIME)
    }
}
