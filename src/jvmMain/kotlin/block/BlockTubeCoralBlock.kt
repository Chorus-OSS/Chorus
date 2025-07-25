package org.chorus_oss.chorus.block

class BlockTubeCoralBlock(blockState: BlockState = properties.defaultState) : BlockCoralBlock(blockState) {
    override fun toDead(): BlockCoralBlock {
        return BlockDeadTubeCoralBlock()
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.TUBE_CORAL_BLOCK)
    }
}