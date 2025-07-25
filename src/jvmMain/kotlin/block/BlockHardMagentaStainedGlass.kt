package org.chorus_oss.chorus.block

class BlockHardMagentaStainedGlass(blockstate: BlockState) : Block(blockstate) {

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.HARD_MAGENTA_STAINED_GLASS)
    }
}