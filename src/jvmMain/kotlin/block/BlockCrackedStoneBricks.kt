package org.chorus_oss.chorus.block

class BlockCrackedStoneBricks @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockStoneBricks(blockstate) {

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.CRACKED_STONE_BRICKS)
    }
}
