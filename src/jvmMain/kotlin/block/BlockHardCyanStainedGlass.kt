package org.chorus_oss.chorus.block

class BlockHardCyanStainedGlass(blockstate: BlockState) : Block(blockstate) {

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.HARD_CYAN_STAINED_GLASS)
    }
}