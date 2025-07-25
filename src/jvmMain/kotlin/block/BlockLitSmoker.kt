package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.blockentity.BlockEntityID
import org.chorus_oss.chorus.blockentity.BlockEntitySmoker
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemBlock

open class BlockLitSmoker @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockLitFurnace(blockstate) {
    override val name: String
        get() = "Burning Smoker"

    override fun getBlockEntityType(): String {
        return BlockEntityID.SMOKER
    }

    override fun getBlockEntityClass() = BlockEntitySmoker::class.java

    override fun toItem(): Item {
        return ItemBlock(BlockSmoker.properties.defaultState, "")
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.LIT_SMOKER, CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION)
    }
}