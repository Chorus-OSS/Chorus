package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.math.BlockFace

class BlockMelonStem @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockCropsStem(blockstate) {
    override val name: String
        get() = "Melon Stem"

    override val fruitId: String
        get() = BlockID.MELON_BLOCK

    override val seedsId: String
        get() = ItemID.MELON_SEEDS

    override val strippedState: BlockState
        get() = BlockStrippedAcaciaLog.properties.defaultState

    override var blockFace: BlockFace
        get() = facing
        set(face) {
            setPropertyValue(CommonBlockProperties.FACING_DIRECTION, face.index)
        }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.MELON_STEM, CommonBlockProperties.FACING_DIRECTION, CommonBlockProperties.GROWTH)
    }
}