package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockPolishedBlackstoneBrickDoubleSlab @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockPolishedBlackstoneDoubleSlab(blockstate) {
    override fun getSlabName() = "Polished Blackstone Brick"

    override val hardness: Double
        get() = 2.0

    override fun getSingleSlab() = BlockPolishedBlackstoneBrickSlab.properties.defaultState

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.POLISHED_BLACKSTONE_BRICK_DOUBLE_SLAB,
            CommonBlockProperties.MINECRAFT_VERTICAL_HALF
        )
    }
}