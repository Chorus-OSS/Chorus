package org.chorus_oss.chorus.block

class BlockHardBlueStainedGlassPane(blockstate: BlockState) : Block(blockstate) {

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.HARD_BLUE_STAINED_GLASS_PANE)
    }
}