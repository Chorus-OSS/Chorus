package org.chorus_oss.chorus.block

class BlockHardPurpleStainedGlassPane(blockstate: BlockState) : Block(blockstate) {

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.HARD_PURPLE_STAINED_GLASS_PANE)
    }
}