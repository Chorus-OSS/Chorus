package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.blockentity.BlockEntityBeacon
import org.chorus_oss.chorus.blockentity.BlockEntityID
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.math.BlockFace

class BlockBeacon @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockTransparent(blockstate), BlockEntityHolder<BlockEntityBeacon> {
    override fun getBlockEntityClass(): Class<out BlockEntityBeacon> {
        return BlockEntityBeacon::class.java
    }

    override fun getBlockEntityType(): String {
        return BlockEntityID.BEACON
    }

    override val hardness: Double
        get() = 3.0

    override fun calculateBreakTime(item: Item): Double {
        return calculateBreakTime(item, null)
    }

    override fun calculateBreakTime(item: Item, player: Player?): Double {
        return 4.5
    }

    override val resistance: Double
        get() = 15.0

    override val lightLevel: Int
        get() = 15

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override val name: String
        get() = "Beacon"

    override fun canBeActivated(): Boolean {
        return true
    }

    override val waterloggingLevel: Int
        get() = 1

    override fun onActivate(
        item: Item,
        player: Player?,
        blockFace: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float
    ): Boolean {
        if (isNotActivate(player)) return false
        val entity = getOrCreateBlockEntity()
        player!!.addWindow(entity.inventory)
        return true
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

    override fun canBePushed(): Boolean {
        return false
    }

    override fun canBePulled(): Boolean {
        return false
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.BEACON)
    }
}
