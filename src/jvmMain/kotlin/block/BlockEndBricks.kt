package org.chorus_oss.chorus.block

class BlockEndBricks @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockEndStone(blockstate) {
    override val name: String
        get() = "End Stone Bricks"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.END_BRICKS)
    }
}