package org.chorus_oss.chorus.block

class BlockHardYellowStainedGlass(blockstate: BlockState) : Block(blockstate) {

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.HARD_YELLOW_STAINED_GLASS)
    }
}