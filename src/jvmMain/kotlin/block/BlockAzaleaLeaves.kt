package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.WoodType
import org.chorus_oss.chorus.item.Item

open class BlockAzaleaLeaves @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockLeaves(blockState) {
    override val name: String
        get() = "Azalea Leaves"

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override fun canHarvest(item: Item): Boolean {
        return item.isShears
    }

    /*这里写木质类型为OAK只是为了获取凋落物时的概率正确，并不代表真的就是橡木*/
    override fun getType(): WoodType {
        return WoodType.OAK
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(
                BlockID.AZALEA_LEAVES,
                CommonBlockProperties.PERSISTENT_BIT,
                CommonBlockProperties.UPDATE_BIT
            )
    }
}
