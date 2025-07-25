package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.blockentity.BlockEntityEnchantTable
import org.chorus_oss.chorus.blockentity.BlockEntityID
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.StringTag

class BlockEnchantingTable @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockTransparent(blockstate), BlockEntityHolder<BlockEntityEnchantTable> {
    override val name: String
        get() = "Enchanting Table"

    override fun getBlockEntityType(): String = BlockEntityID.ENCHANT_TABLE

    override fun getBlockEntityClass(): Class<out BlockEntityEnchantTable> = BlockEntityEnchantTable::class.java

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override val hardness: Double
        get() = 5.0

    override val resistance: Double
        get() = 6000.0

    override val waterloggingLevel: Int
        get() = 1

    override val lightLevel: Int
        get() = 7

    override fun canBeActivated(): Boolean {
        return true
    }

    override var maxY: Double
        get() = y + 12 / 16.0
        set(maxY) {
            super.maxY = maxY
        }

    override val toolTier: Int
        get() = ItemTool.TIER_WOODEN

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

        return BlockEntityHolder.setBlockAndCreateEntity(
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
        if (player == null) {
            return true
        }
        val itemInHand = player.inventory.itemInHand
        if (player.isSneaking() && !(itemInHand.isTool || itemInHand.isNothing)) {
            return false
        }

        val enchantTable = getOrCreateBlockEntity()
        if (enchantTable.namedTag.contains("Lock") && enchantTable.namedTag["Lock"] is StringTag
            && (enchantTable.namedTag.getString("Lock") != item.customName)
        ) {
            return false
        }

        player.addWindow(enchantTable.inventory)

        return true
    }

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override fun isSolid(side: BlockFace): Boolean {
        return false
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.ENCHANTING_TABLE)
    }
}
