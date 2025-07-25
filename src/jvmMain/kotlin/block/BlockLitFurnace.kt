package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.CommonPropertyMap
import org.chorus_oss.chorus.blockentity.BlockEntityFurnace
import org.chorus_oss.chorus.blockentity.BlockEntityID
import org.chorus_oss.chorus.inventory.ContainerInventory.Companion.calculateRedstone
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemBlock
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockFace.Companion.fromHorizontalIndex
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.nbt.tag.StringTag
import org.chorus_oss.chorus.nbt.tag.Tag
import org.chorus_oss.chorus.utils.Faceable

open class BlockLitFurnace @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockSolid(blockstate), Faceable, BlockEntityHolder<BlockEntityFurnace> {
    override val name: String
        get() = "Burning Furnace"

    override fun getBlockEntityClass(): Class<out BlockEntityFurnace> = BlockEntityFurnace::class.java

    override fun getBlockEntityType(): String = BlockEntityID.FURNACE

    override fun canBeActivated(): Boolean {
        return true
    }

    override val hardness: Double
        get() = 3.5

    override val resistance: Double
        get() = 17.5

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override val lightLevel: Int
        get() = 13

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

        val nbt = CompoundTag().putList("Items", ListTag<Tag<*>>())

        if (item!!.hasCustomName()) {
            nbt.putString("CustomName", item.customName)
        }

        if (item.hasCustomBlockData()) {
            val customData = item.customBlockData!!.tags
            for ((key, value) in customData) {
                nbt.put(key, value)
            }
        }

        return BlockEntityHolder.setBlockAndCreateEntity(this, direct = true, update = true, initialData = nbt) != null
    }

    override fun onBreak(item: Item?): Boolean {
        level.setBlock(this.position, get(BlockID.AIR), direct = true, update = true)
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
        if (player == null) {
            return false
        }
        val itemInHand = player.inventory.itemInHand
        if (player.isSneaking() && !(itemInHand.isTool || itemInHand.isNothing)) {
            return false
        }

        val furnace = getOrCreateBlockEntity()
        if (furnace.namedTag.contains("Lock") && furnace.namedTag.get("Lock") is StringTag
            && (furnace.namedTag.getString("Lock") != item.customName)
        ) {
            return false
        }

        player.addWindow(furnace.inventory)
        return true
    }

    override fun toItem(): Item {
        return ItemBlock(BlockFurnace.properties.defaultState, "")
    }

    override val toolTier: Int
        get() = ItemTool.TIER_WOODEN

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

    override fun canHarvestWithHand(): Boolean {
        return false
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

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.LIT_FURNACE, CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION)
    }
}