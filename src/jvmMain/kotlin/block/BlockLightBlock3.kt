package org.chorus_oss.chorus.block

class BlockLightBlock3 @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockLightBlock0(blockstate) {
    override val lightLevel: Int
        get() = 3

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.LIGHT_BLOCK_3)
    }
}