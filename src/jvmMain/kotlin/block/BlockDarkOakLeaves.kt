package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.WoodType
import org.chorus_oss.chorus.item.Item

class BlockDarkOakLeaves(blockstate: BlockState) : BlockLeaves(blockstate) {
    override fun getType(): WoodType {
        return WoodType.DARK_OAK
    }

    override fun toSapling(): Item {
        return Item.get(BlockID.DARK_OAK_SAPLING)
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(
                BlockID.DARK_OAK_LEAVES,
                CommonBlockProperties.PERSISTENT_BIT,
                CommonBlockProperties.UPDATE_BIT
            )
    }
}