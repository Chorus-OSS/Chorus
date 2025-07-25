package org.chorus_oss.chorus.block

class BlockHardGrayStainedGlass(blockstate: BlockState) : Block(blockstate) {

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.HARD_GRAY_STAINED_GLASS)
    }
}