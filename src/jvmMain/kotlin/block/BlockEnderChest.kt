package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.CommonPropertyMap
import org.chorus_oss.chorus.blockentity.BlockEntityEnderChest
import org.chorus_oss.chorus.blockentity.BlockEntityID
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.Item.Companion.get
import org.chorus_oss.chorus.item.ItemBlock
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockFace.Companion.fromHorizontalIndex
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.StringTag
import org.chorus_oss.chorus.utils.Faceable

class BlockEnderChest @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockTransparent(blockstate), Faceable, BlockEntityHolder<BlockEntityEnderChest> {
    override fun canBeActivated(): Boolean {
        return true
    }

    override fun getBlockEntityType(): String {
        return BlockEntityID.ENDER_CHEST
    }

    override fun getBlockEntityClass() = BlockEntityEnderChest::class.java

    override val lightLevel: Int
        get() = 7

    override val waterloggingLevel: Int
        get() = 1

    override val name: String
        get() = "Ender Chest"

    override val hardness: Double
        get() = 22.5

    override val resistance: Double
        get() = 3000.0

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override var minX: Double
        get() = position.x + 0.0625
        set(minX) {
            super.minX = minX
        }

    override var minZ: Double
        get() = position.z + 0.0625
        set(minZ) {
            super.minZ = minZ
        }

    override var maxX: Double
        get() = position.x + 0.9375
        set(maxX) {
            super.maxX = maxX
        }

    override var maxY: Double
        get() = position.y + 0.9475
        set(maxY) {
            super.maxY = maxY
        }

    override var maxZ: Double
        get() = position.z + 0.9375
        set(maxZ) {
            super.maxZ = maxZ
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
        blockFace = if (player != null) fromHorizontalIndex(
            player.getDirection().getOpposite().horizontalIndex
        ) else BlockFace.SOUTH

        val nbt = CompoundTag()

        if (item!!.hasCustomName()) {
            nbt.putString("CustomName", item.customName)
        }

        if (item.hasCustomBlockData()) {
            val customData = item.customBlockData!!.tags
            for ((key, value) in customData) {
                nbt.put(key, value)
            }
        }

        return BlockEntityHolder.Companion.setBlockAndCreateEntity(
            this,
            direct = true,
            update = true,
            initialData = nbt
        ) != null
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

        val top = this.up()
        if (!top.isTransparent) {
            return false
        }

        val chest = getOrCreateBlockEntity()
        if (chest.namedTag.contains("Lock") && chest.namedTag["Lock"] is StringTag
            && (chest.namedTag.getString("Lock") != item.customName)
        ) {
            return false
        }

        val enderChestInventory = player!!.enderChestInventory
        enderChestInventory.setBlockEntityEnderChest(player, chest)
        player.addWindow(enderChestInventory)
        return true
    }

    override val toolTier: Int
        get() = ItemTool.TIER_WOODEN

    override fun getDrops(item: Item): Array<Item> {
        return if (item.isPickaxe && item.tier >= toolTier) {
            arrayOf(
                get(get(BlockID.OBSIDIAN).itemId, 0, 8)
            )
        } else {
            Item.EMPTY_ARRAY
        }
    }

    override fun canBePushed(): Boolean {
        return false
    }

    override fun canBePulled(): Boolean {
        return false
    }

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override fun canSilkTouch(): Boolean {
        return true
    }

    override fun toItem(): Item {
        return ItemBlock(this.blockState, name, 0)
    }

    override var blockFace: BlockFace
        get() = CommonPropertyMap.CARDINAL_BLOCKFACE[getPropertyValue(
            CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION
        )]!!
        set(face) {
            this.setPropertyValue(
                CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION,
                CommonPropertyMap.CARDINAL_BLOCKFACE.inverse()[face]!!
            )
        }

    override val blockEntity: BlockEntityEnderChest?
        get() = getTypedBlockEntity(BlockEntityEnderChest::class.java)

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.ENDER_CHEST, CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION)
    }
}
