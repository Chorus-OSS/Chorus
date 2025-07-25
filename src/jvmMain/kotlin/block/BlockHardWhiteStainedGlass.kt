package org.chorus_oss.chorus.block

class BlockHardWhiteStainedGlass(blockstate: BlockState) : Block(blockstate) {

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.HARD_WHITE_STAINED_GLASS)
    }
}