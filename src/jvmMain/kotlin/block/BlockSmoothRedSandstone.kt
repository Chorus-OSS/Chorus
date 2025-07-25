package org.chorus_oss.chorus.block

class BlockSmoothRedSandstone @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockSmoothSandstone(blockstate) {

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.SMOOTH_RED_SANDSTONE)
    }
}
