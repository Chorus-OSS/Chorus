package org.chorus_oss.chorus.block

class BlockFireCoralBlock(blockState: BlockState = properties.defaultState) : BlockCoralBlock(blockState) {
    override fun toDead(): BlockCoralBlock {
        return BlockDeadFireCoralBlock()
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.FIRE_CORAL_BLOCK)
    }
}