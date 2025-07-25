package org.chorus_oss.chorus.block

class BlockMossyStoneBricks @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockStoneBricks(blockstate) {

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.MOSSY_STONE_BRICKS)
    }
}
