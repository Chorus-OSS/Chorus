package org.chorus_oss.chorus.block

class BlockHardLightGrayStainedGlass(blockstate: BlockState) : Block(blockstate) {

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.HARD_LIGHT_GRAY_STAINED_GLASS)
    }
}