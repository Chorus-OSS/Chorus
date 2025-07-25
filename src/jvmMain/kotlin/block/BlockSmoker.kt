package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties


class BlockSmoker @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockLitSmoker(blockstate) {
    override val name: String
        get() = "Smoker"

    override val lightLevel: Int
        get() = 0

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.SMOKER, CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION)
    }
}
