package org.chorus_oss.chorus.block

class BlockHardBrownStainedGlass(blockstate: BlockState) : Block(blockstate) {

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.HARD_BROWN_STAINED_GLASS)
    }
}