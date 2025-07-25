package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.Attachment
import org.chorus_oss.chorus.inventory.BlockInventoryHolder
import org.chorus_oss.chorus.inventory.GrindstoneInventory
import org.chorus_oss.chorus.inventory.Inventory
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockFace.Companion.fromHorizontalIndex
import org.chorus_oss.chorus.math.SimpleAxisAlignedBB
import org.chorus_oss.chorus.utils.Faceable
import java.util.function.Supplier

class BlockGrindstone @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockTransparent(blockstate), Faceable, BlockInventoryHolder {
    override val name: String
        get() = "Grindstone"

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override val toolTier: Int
        get() = ItemTool.TIER_WOODEN

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override val waterloggingLevel: Int
        get() = 1

    override val hardness: Double
        get() = 2.0

    override val resistance: Double
        get() = 6.0

    override var blockFace: BlockFace
        get() = fromHorizontalIndex(getPropertyValue(CommonBlockProperties.DIRECTION))
        set(face) {
            if (face.horizontalIndex == -1) {
                return
            }
            setPropertyValue(CommonBlockProperties.DIRECTION, face.horizontalIndex)
        }

    var attachmentType: Attachment
        get() = getPropertyValue(
            CommonBlockProperties.ATTACHMENT
        )
        set(attachmentType) {
            setPropertyValue(
                CommonBlockProperties.ATTACHMENT,
                attachmentType
            )
        }

    private fun isConnectedTo(connectedFace: BlockFace, attachmentType: Attachment, blockFace: BlockFace): Boolean {
        val faceAxis = connectedFace.axis
        return when (attachmentType) {
            Attachment.STANDING -> {
                if (faceAxis == BlockFace.Axis.Y) {
                    connectedFace == BlockFace.DOWN
                } else {
                    false
                }
            }

            Attachment.HANGING -> {
                connectedFace == BlockFace.UP
            }

            Attachment.SIDE, Attachment.MULTIPLE -> {
                connectedFace == blockFace.getOpposite()
            }
        }
    }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (!checkSupport()) {
                level.useBreakOn(this.position, Item.get(ItemID.DIAMOND_PICKAXE))
            }
            return type
        }
        return 0
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
        var face1 = face
        if (!block.isAir && block.canBeReplaced()) {
            face1 = BlockFace.UP
        }
        when (face1) {
            BlockFace.UP -> {
                attachmentType = Attachment.STANDING
                blockFace = player!!.getDirection().getOpposite()
            }

            BlockFace.DOWN -> {
                attachmentType = Attachment.HANGING
                blockFace = player!!.getDirection().getOpposite()
            }

            else -> {
                blockFace = face1
                attachmentType = Attachment.SIDE
            }
        }
        if (!checkSupport()) {
            return false
        }
        level.setBlock(this.position, this, true, true)
        return true
    }

    private fun checkSupport(): Boolean {
        return when (attachmentType) {
            Attachment.STANDING -> checkSupport(down())
            Attachment.HANGING -> checkSupport(up())
            Attachment.SIDE -> checkSupport(getSide(blockFace.getOpposite()))
            else -> false
        }
    }

    private fun checkSupport(support: Block): Boolean {
        val id = support.id
        return (id != BlockID.AIR) && (id != BlockID.BUBBLE_COLUMN) && (support !is BlockLiquid)
    }

    override fun recalculateBoundingBox(): AxisAlignedBB {
        val blockFace = blockFace
        val south = this.isConnectedTo(BlockFace.SOUTH, attachmentType, blockFace)
        val north = this.isConnectedTo(BlockFace.NORTH, attachmentType, blockFace)
        val west = this.isConnectedTo(BlockFace.WEST, attachmentType, blockFace)
        val east = this.isConnectedTo(BlockFace.EAST, attachmentType, blockFace)
        val up = this.isConnectedTo(BlockFace.UP, attachmentType, blockFace)
        val down = this.isConnectedTo(BlockFace.DOWN, attachmentType, blockFace)

        val pixels = (2.0 / 16)

        val n = if (north) 0.0 else pixels
        val s = if (south) 1.0 else 1 - pixels
        val w = if (west) 0.0 else pixels
        val e = if (east) 1.0 else 1 - pixels
        val d = if (down) 0.0 else pixels
        val u = if (up) 1.0 else 1 - pixels

        return SimpleAxisAlignedBB(
            position.x + w,
            position.y + d,
            position.z + n,
            position.x + e,
            position.y + u,
            position.z + s
        )
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
        if (player != null) {
            val itemInHand = player.inventory.itemInHand
            if (player.isSneaking() && !(itemInHand.isTool || itemInHand.isNothing)) {
                return false
            }
            player.addWindow(inventory)
        }
        return true
    }

    override fun blockInventorySupplier(): Supplier<Inventory> {
        return Supplier { GrindstoneInventory(this) }
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.GRINDSTONE, CommonBlockProperties.ATTACHMENT, CommonBlockProperties.DIRECTION)
    }
}
