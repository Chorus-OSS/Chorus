package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.blockentity.BlockEntityBarrel
import org.chorus_oss.chorus.blockentity.BlockEntityID
import org.chorus_oss.chorus.inventory.ContainerInventory.Companion.calculateRedstone
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockFace.Companion.fromIndex
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.nbt.tag.StringTag
import org.chorus_oss.chorus.nbt.tag.Tag
import org.chorus_oss.chorus.utils.Faceable
import kotlin.math.abs

class BlockBarrel @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockSolid(blockState), Faceable, BlockEntityHolder<BlockEntityBarrel> {
    override val name: String
        get() = "Barrel"

    override fun getBlockEntityType(): String {
        return BlockEntityID.BARREL
    }

    override fun getBlockEntityClass(): Class<out BlockEntityBarrel> {
        return BlockEntityBarrel::class.java
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
        if (player == null) {
            blockFace = BlockFace.UP
        } else {
            if (abs(player.position.x - position.x) < 2 && abs(player.position.z - position.z) < 2) {
                val y = player.position.y + player.getEyeHeight()

                blockFace = if (y - position.y > 2) {
                    BlockFace.UP
                } else if (position.y - y > 0) {
                    BlockFace.DOWN
                } else {
                    player.getHorizontalFacing().getOpposite()
                }
            } else {
                blockFace = player.getHorizontalFacing().getOpposite()
            }
        }

        val nbt = CompoundTag().putList("Items", ListTag<Tag<*>>())

        if (item!!.hasCustomName()) {
            nbt.putString("CustomName", item.customName)
        }

        if (item.hasCustomBlockData()) {
            val customData: Map<String, Tag<*>> = item.customBlockData!!.tags
            for ((key, value) in customData) {
                nbt.put(key, value)
            }
        }

        return BlockEntityHolder.setBlockAndCreateEntity(this, direct = true, update = true, initialData = nbt) != null
    }

    override fun onActivate(
        item: Item,
        player: Player?,
        blockFace: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float
    ): Boolean {
        if (isNotActivate(player)) return false

        val barrel = getOrCreateBlockEntity()

        if (barrel.namedTag.contains("Lock") && barrel.namedTag["Lock"] is StringTag
            && (barrel.namedTag.getString("Lock") != item.customName)
        ) {
            return false
        }

        player?.addWindow(barrel.inventory)
        return true
    }

    override fun canBeActivated(): Boolean {
        return true
    }

    override val hardness: Double
        get() = 2.5

    override val resistance: Double
        get() = 12.5

    override val toolType: Int
        get() = ItemTool.TYPE_AXE

    override var blockFace: BlockFace
        get() = fromIndex(getPropertyValue(CommonBlockProperties.FACING_DIRECTION))
        set(face) {
            setPropertyValue(CommonBlockProperties.FACING_DIRECTION, face.index)
        }

    var isOpen: Boolean
        get() = getPropertyValue(CommonBlockProperties.OPEN_BIT)
        set(open) {
            setPropertyValue(CommonBlockProperties.OPEN_BIT, open)
        }

    override fun hasComparatorInputOverride(): Boolean {
        return true
    }

    override val comparatorInputOverride: Int
        get() {
            val blockEntity = blockEntity

            if (blockEntity != null) {
                return calculateRedstone(blockEntity.inventory)
            }

            return super.comparatorInputOverride
        }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.BARREL, CommonBlockProperties.FACING_DIRECTION, CommonBlockProperties.OPEN_BIT)
    }
}
