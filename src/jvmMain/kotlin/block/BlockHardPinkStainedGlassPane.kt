package org.chorus_oss.chorus.block

class BlockHardPinkStainedGlassPane(blockstate: BlockState) : Block(blockstate) {

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.HARD_PINK_STAINED_GLASS_PANE)
    }
}