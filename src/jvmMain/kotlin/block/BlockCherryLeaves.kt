package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.WoodType
import org.chorus_oss.chorus.item.Item

class BlockCherryLeaves @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockLeaves(blockState) {
    override val name: String
        get() = "Cherry Leaves"

    /*这里写木质类型为BIRCH只是为了获取凋落物时的概率正确，并不代表真的就是白桦木*/
    override fun getType(): WoodType {
        return WoodType.CHERRY
    }

    override fun toSapling(): Item {
        return Item.get(BlockID.CHERRY_SAPLING)
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(
                BlockID.CHERRY_LEAVES,
                CommonBlockProperties.PERSISTENT_BIT,
                CommonBlockProperties.UPDATE_BIT
            )
    }
}
