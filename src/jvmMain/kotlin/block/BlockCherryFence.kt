package org.chorus_oss.chorus.block

class BlockCherryFence @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockFence(blockState) {
    override val name: String
        get() = "Cherry Fence"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.CHERRY_FENCE)
    }
}