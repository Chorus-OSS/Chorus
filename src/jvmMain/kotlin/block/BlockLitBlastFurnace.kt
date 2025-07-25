package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.blockentity.BlockEntityBlastFurnace
import org.chorus_oss.chorus.blockentity.BlockEntityFurnace
import org.chorus_oss.chorus.blockentity.BlockEntityID
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemBlock

open class BlockLitBlastFurnace @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockLitFurnace(blockstate) {
    override val name: String
        get() = "Burning Blast Furnace"

    override fun getBlockEntityType(): String = BlockEntityID.BLAST_FURNACE

    override fun getBlockEntityClass(): Class<out BlockEntityFurnace> = BlockEntityBlastFurnace::class.java

    override fun toItem(): Item {
        return ItemBlock(BlockBlastFurnace.properties.defaultState, "")
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.LIT_BLAST_FURNACE, CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION)

    }
}