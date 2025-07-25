package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.CommonPropertyMap
import org.chorus_oss.chorus.inventory.BlockInventoryHolder
import org.chorus_oss.chorus.inventory.Inventory
import org.chorus_oss.chorus.inventory.StonecutterInventory
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockFace.Companion.fromHorizontalIndex
import org.chorus_oss.chorus.utils.Faceable
import java.util.function.Supplier

class BlockStonecutterBlock @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockTransparent(blockstate), Faceable, BlockInventoryHolder {
    override val name: String
        get() = "Stonecutter"

    override var blockFace: BlockFace
        get() = CommonPropertyMap.CARDINAL_BLOCKFACE[getPropertyValue(
            CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION
        )]!!
        set(face) {
            val horizontalIndex = face.horizontalIndex
            if (horizontalIndex > -1) {
                this.setPropertyValue(
                    CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION,
                    CommonPropertyMap.CARDINAL_BLOCKFACE.inverse()[fromHorizontalIndex(horizontalIndex)]!!
                )
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
        blockFace =
            if (player != null) fromHorizontalIndex(player.getDirection().horizontalIndex) else BlockFace.SOUTH

        level.setBlock(block.position, this, true, true)
        return true
    }

    override fun canBeActivated(): Boolean {
        return true
    }

    override fun onActivate(
        item: Item,
        player: Player?,
        blockFace: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float
    ): Boolean {
        player?.addWindow(inventory)
        return true
    }

    override val hardness: Double
        get() = 3.5

    override val resistance: Double
        get() = 17.5

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override val waterloggingLevel: Int
        get() = 1

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override val toolTier: Int
        get() = ItemTool.TIER_WOODEN

    override fun getDrops(item: Item): Array<Item> {
        return arrayOf(toItem())
    }

    override var maxY: Double
        get() = position.y + 9 / 16.0
        set(maxY) {
            super.maxY = maxY
        }

    override fun blockInventorySupplier(): Supplier<Inventory> {
        return Supplier { StonecutterInventory(this) }
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.STONECUTTER_BLOCK, CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION)
    }
}
