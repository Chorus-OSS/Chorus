package org.chorus_oss.chorus.block

class BlockDeadTubeCoralBlock(blockState: BlockState = properties.defaultState) : BlockCoralBlock(blockState) {
    override val isDead: Boolean
        get() = true

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.DEAD_TUBE_CORAL_BLOCK)
    }
}