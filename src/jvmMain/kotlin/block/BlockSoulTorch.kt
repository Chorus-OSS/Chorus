package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties


class BlockSoulTorch @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockTorch(blockstate) {
    override val name: String
        get() = "Soul Torch"

    override val lightLevel: Int
        get() = 10

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.SOUL_TORCH, CommonBlockProperties.TORCH_FACING_DIRECTION)
    }
}
