package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.blockentity.BlockEntity
import org.chorus_oss.chorus.blockentity.BlockEntityBrewingStand
import org.chorus_oss.chorus.blockentity.BlockEntityID
import org.chorus_oss.chorus.inventory.ContainerInventory.Companion.calculateRedstone
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.nbt.tag.StringTag
import org.chorus_oss.chorus.nbt.tag.Tag

class BlockBrewingStand @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockTransparent(blockstate), BlockEntityHolder<BlockEntityBrewingStand> {
    override val name: String
        get() = "Brewing Stand"

    override fun canBeActivated(): Boolean {
        return true
    }

    override val hardness: Double
        get() = 0.5

    override val resistance: Double
        get() = 2.5

    override val waterloggingLevel: Int
        get() = 1

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override val lightLevel: Int
        get() = 1

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
        level.setBlock(block.position, this, direct = true, update = true)

        val nbt = CompoundTag()
            .putList("Items", ListTag<Tag<*>>())
            .putString("id", BlockEntityID.BREWING_STAND)
            .putInt("x", position.x.toInt())
            .putInt("y", position.y.toInt())
            .putInt("z", position.z.toInt())

        if (item!!.hasCustomName()) {
            nbt.putString("CustomName", item.customName)
        }

        if (item.hasCustomBlockData()) {
            val customData: Map<String, Tag<*>> = item.customBlockData!!.tags
            for ((key, value) in customData) {
                nbt.put(key, value)
            }
        }

        val brewing = BlockEntity.createBlockEntity(
            BlockEntityID.BREWING_STAND, level.getChunk(
                position.x.toInt() shr 4, position.z.toInt() shr 4
            ), nbt
        ) as BlockEntityBrewingStand?
        return brewing != null
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
            val t = level.getBlockEntity(this.position)
            val brewing: BlockEntityBrewingStand?
            if (t is BlockEntityBrewingStand) {
                brewing = t
            } else {
                val nbt = CompoundTag()
                    .putList("Items", ListTag<Tag<*>>())
                    .putString("id", BlockEntityID.BREWING_STAND)
                    .putInt("x", position.x.toInt())
                    .putInt("y", position.y.toInt())
                    .putInt("z", position.z.toInt())
                brewing = BlockEntity.createBlockEntity(
                    BlockEntityID.BREWING_STAND,
                    level.getChunk(position.x.toInt() shr 4, position.z.toInt() shr 4), nbt
                ) as BlockEntityBrewingStand?
                if (brewing == null) {
                    return false
                }
            }

            if (brewing.namedTag.contains("Lock") && brewing.namedTag["Lock"] is StringTag) {
                if (brewing.namedTag.getString("Lock") != item.customName) {
                    return false
                }
            }

            player.addWindow(brewing.inventory)
        }

        return true
    }

    override val toolTier: Int
        get() = ItemTool.TIER_WOODEN

    override val isSolid: Boolean
        get() = false

    override var minX: Double
        get() = position.x + 7 / 16.0
        set(minX) {
            super.minX = minX
        }

    override var minZ: Double
        get() = position.z + 7 / 16.0
        set(minZ) {
            super.minZ = minZ
        }

    override var maxX: Double
        get() = position.x + 1 - 7 / 16.0
        set(maxX) {
            super.maxX = maxX
        }

    override var maxY: Double
        get() = position.y + 1 - 2 / 16.0
        set(maxY) {
            super.maxY = maxY
        }

    override var maxZ: Double
        get() = position.z + 1 - 7 / 16.0
        set(maxZ) {
            super.maxZ = maxZ
        }

    override fun hasComparatorInputOverride(): Boolean {
        return true
    }

    override val comparatorInputOverride: Int
        get() {
            val blockEntity =
                level.getBlockEntity(this.position)

            if (blockEntity is BlockEntityBrewingStand) {
                return calculateRedstone(blockEntity.inventory)
            }

            return super.comparatorInputOverride
        }

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override fun getBlockEntityClass(): Class<out BlockEntityBrewingStand> {
        return BlockEntityBrewingStand::class.java
    }

    override fun getBlockEntityType(): String {
        return BlockEntityID.BREWING_STAND
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.BREWING_STAND,
            CommonBlockProperties.BREWING_STAND_SLOT_A_BIT,
            CommonBlockProperties.BREWING_STAND_SLOT_B_BIT,
            CommonBlockProperties.BREWING_STAND_SLOT_C_BIT
        )
    }
}
