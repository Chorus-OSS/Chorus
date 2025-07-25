package org.chorus_oss.chorus.block

class BlockHardCyanStainedGlassPane(blockstate: BlockState) : Block(blockstate) {

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.HARD_CYAN_STAINED_GLASS_PANE)
    }
}