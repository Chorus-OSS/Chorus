package org.chorus_oss.chorus.block

class BlockPaleOakFence @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockFence(blockstate) {
    override val name: String
        get() = "Pale Oak Fence"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.PALE_OAK_FENCE)
    }
}