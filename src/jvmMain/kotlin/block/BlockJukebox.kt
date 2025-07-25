package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.blockentity.BlockEntityID
import org.chorus_oss.chorus.blockentity.BlockEntityJukebox
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemBlock
import org.chorus_oss.chorus.item.ItemMusicDisc
import org.chorus_oss.chorus.math.BlockFace

class BlockJukebox @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockSolid(blockstate), BlockEntityHolder<BlockEntityJukebox> {
    override val name: String
        get() = "Jukebox"

    override fun getBlockEntityClass() = BlockEntityJukebox::class.java

    override fun getBlockEntityType(): String {
        return BlockEntityID.JUKEBOX
    }

    override fun canBeActivated(): Boolean {
        return true
    }

    override fun toItem(): Item {
        return ItemBlock(this.blockState, name, 0)
    }

    override val hardness: Double
        get() = 1.0

    override fun onActivate(
        item: Item,
        player: Player?,
        blockFace: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float
    ): Boolean {
        val jukebox = getOrCreateBlockEntity()
        if (!jukebox.getRecordItem().isNothing) {
            jukebox.dropItem()
            return true
        }

        if (!item.isNothing && item is ItemMusicDisc) {
            val record: Item = item.clone()
            record.count = 1
            item.count--
            jukebox.setRecordItem(record)
            jukebox.play()
            return true
        }

        return false
    }

    override fun place(
        item: Item?,
        block: Block,
        target: Block?,
        face: BlockFace,
        fx: Double,
        fy: Double,
        fz: Double,
        player: Player?
    ): Boolean {
        return BlockEntityHolder.setBlockAndCreateEntity(this) != null
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.JUKEBOX)
    }
}
