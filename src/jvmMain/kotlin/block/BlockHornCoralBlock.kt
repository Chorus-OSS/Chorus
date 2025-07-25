package org.chorus_oss.chorus.block

class BlockHornCoralBlock(blockState: BlockState = properties.defaultState) : BlockCoralBlock(blockState) {
    override fun toDead(): BlockCoralBlock {
        return BlockDeadHornCoralBlock()
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.HORN_CORAL_BLOCK)
    }
}