package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemBambooSign

class BlockBambooWallSign @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockWallSign(blockState) {
    override val name: String
        get() = "Bamboo Wall Sign"

    override fun getStandingSignId(): String {
        return BlockID.BAMBOO_STANDING_SIGN
    }

    override fun getWallSignId(): String {
        return BlockAcaciaWallSign.properties.identifier
    }

    override fun toItem(): Item {
        return ItemBambooSign()
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.BAMBOO_WALL_SIGN, CommonBlockProperties.FACING_DIRECTION)
    }
}