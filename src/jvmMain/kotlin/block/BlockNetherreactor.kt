package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.blockentity.BlockEntityID
import org.chorus_oss.chorus.blockentity.BlockEntityNetherReactor
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.Item.Companion.get
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.math.BlockFace

class BlockNetherreactor @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockSolid(blockstate), BlockEntityHolder<BlockEntityNetherReactor> {
    override fun getBlockEntityType(): String {
        return BlockEntityID.NETHER_REACTOR
    }

    override fun getBlockEntityClass() = BlockEntityNetherReactor::class.java

    override val name: String
        get() = "Nether Reactor Core"

    override val hardness: Double
        get() = 10.0

    override val resistance: Double
        get() = 6.0

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override val toolTier: Int
        get() = ItemTool.TIER_WOODEN

    override fun getDrops(item: Item): Array<Item> {
        return if (item.isPickaxe) {
            arrayOf(
                get(ItemID.DIAMOND, 0, 3),
                get(ItemID.IRON_INGOT, 0, 6)
            )
        } else {
            Item.EMPTY_ARRAY
        }
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
        val properties: BlockProperties = BlockProperties(BlockID.NETHERREACTOR)
    }
}
