package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockColoredTorchRg @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    Block(blockstate) {

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.COLORED_TORCH_RG,
            CommonBlockProperties.COLOR_BIT,
            CommonBlockProperties.TORCH_FACING_DIRECTION
        )
    }
}