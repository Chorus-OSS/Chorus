package org.chorus_oss.chorus.block

class BlockBubbleCoralBlock(blockState: BlockState = properties.defaultState) : BlockCoralBlock(blockState) {
    override fun toDead(): BlockCoralBlock {
        return BlockDeadBubbleCoralBlock()
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.BUBBLE_CORAL_BLOCK)
    }
}