package org.chorus_oss.chorus.block

class BlockHardBlackStainedGlass(blockstate: BlockState) : Block(blockstate) {

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.HARD_BLACK_STAINED_GLASS)
    }
}