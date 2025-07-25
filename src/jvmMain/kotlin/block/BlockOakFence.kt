package org.chorus_oss.chorus.block

class BlockOakFence @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockFence(blockstate) {
    override val name: String
        get() = "Oak Fence"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.OAK_FENCE)
    }
}