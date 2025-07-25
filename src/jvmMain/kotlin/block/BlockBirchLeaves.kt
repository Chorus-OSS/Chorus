package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.WoodType
import org.chorus_oss.chorus.item.Item

class BlockBirchLeaves(blockstate: BlockState) : BlockLeaves(blockstate) {
    override fun getType(): WoodType {
        return WoodType.BIRCH
    }

    override fun toSapling(): Item {
        return Item.get(BlockID.BIRCH_SAPLING)
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(
                BlockID.BIRCH_LEAVES,
                CommonBlockProperties.PERSISTENT_BIT,
                CommonBlockProperties.UPDATE_BIT
            )
    }
}