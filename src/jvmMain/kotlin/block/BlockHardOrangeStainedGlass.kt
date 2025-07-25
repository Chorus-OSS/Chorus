package org.chorus_oss.chorus.block

class BlockHardOrangeStainedGlass(blockstate: BlockState) : Block(blockstate) {

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.HARD_ORANGE_STAINED_GLASS)
    }
}