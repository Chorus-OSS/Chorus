package org.chorus_oss.chorus.block

class BlockHardLimeStainedGlassPane(blockstate: BlockState) : Block(blockstate) {

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.HARD_LIME_STAINED_GLASS_PANE)
    }
}