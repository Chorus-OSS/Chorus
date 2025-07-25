package org.chorus_oss.chorus.block

class BlockJungleFence @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockFence(blockstate) {
    override val name: String
        get() = "Jungle Fence"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.JUNGLE_FENCE)
    }
}