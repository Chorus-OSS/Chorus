package org.chorus_oss.chorus.block

class BlockHardRedStainedGlass(blockstate: BlockState) : Block(blockstate) {

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.HARD_RED_STAINED_GLASS)
    }
}