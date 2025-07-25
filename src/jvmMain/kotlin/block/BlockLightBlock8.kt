package org.chorus_oss.chorus.block

class BlockLightBlock8 @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockLightBlock0(blockstate) {
    override val lightLevel: Int
        get() = 8

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.LIGHT_BLOCK_8)
    }
}