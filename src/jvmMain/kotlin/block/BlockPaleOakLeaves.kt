package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.WoodType
import org.chorus_oss.chorus.item.Item

class BlockPaleOakLeaves(blockstate: BlockState) : BlockLeaves(blockstate) {
    override fun getType() = WoodType.PALE_OAK

    override fun toSapling(): Item {
        return Item.get(BlockID.PALE_OAK_SAPLING)
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.PALE_OAK_LEAVES,
            CommonBlockProperties.PERSISTENT_BIT,
            CommonBlockProperties.UPDATE_BIT
        )
    }
}