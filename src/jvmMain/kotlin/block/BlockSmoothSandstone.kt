package org.chorus_oss.chorus.block

open class BlockSmoothSandstone @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockSandstone(blockstate) {
    override val hardness: Double
        get() = 2.0

    override val resistance: Double
        get() = 6.0

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.SMOOTH_SANDSTONE)
    }
}
