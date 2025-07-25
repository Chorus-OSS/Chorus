package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.math.BlockFace

class BlockSmoothBasalt @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockBasalt(blockState) {
    override val name: String
        get() = "Smooth Basalt"

    override var pillarAxis: BlockFace.Axis
        get() = throw UnsupportedOperationException()
        set(axis) = throw UnsupportedOperationException()

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.SMOOTH_BASALT)
    }
}
