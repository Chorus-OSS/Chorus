package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.blockentity.BlockEntityID
import org.chorus_oss.chorus.blockentity.BlockEntityShulkerBox
import org.chorus_oss.chorus.inventory.ContainerInventory.Companion.calculateRedstone
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockFace.Companion.fromIndex
import org.chorus_oss.chorus.nbt.NBTIO.putItemHelper
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.tags.BlockTags

open class BlockUndyedShulkerBox(blockState: BlockState) : BlockTransparent(blockState),
    BlockEntityHolder<BlockEntityShulkerBox> {

    override fun getBlockEntityClass(): Class<out BlockEntityShulkerBox> = BlockEntityShulkerBox::class.java

    override fun getBlockEntityType(): String = BlockEntityID.SHULKER_BOX

    override val hardness: Double
        get() = 2.0

    override val resistance: Double
        get() = 10.0

    override fun canBeActivated(): Boolean {
        return true
    }

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override val waterloggingLevel: Int
        get() = 1

    open fun getShulkerBox(): Item = super.toItem()

    override fun toItem(): Item {
        val item = getShulkerBox()

        val tile = blockEntity ?: return item

        val inv = tile.realInventory

        if (!inv!!.isEmpty) {
            var nbt = item.namedTag
            if (nbt == null) {
                nbt = CompoundTag()
            }

            val items = ListTag<CompoundTag>()

            for (it in 0..<inv.size) {
                if (!inv.getItem(it).isNothing) {
                    val d = putItemHelper(inv.getItem(it), it)
                    items.add(d)
                }
            }

            nbt.put("Items", items)

            item.setCompoundTag(nbt)
        }

        if (tile.hasName()) {
            item.setCustomName(tile.name)
        }

        return item
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
        val nbt = CompoundTag().putByte("facing", face.index)

        if (item!!.hasCustomName()) {
            nbt.putString("CustomName", item.customName)
        }

        val t = item.namedTag

        // This code gets executed when the player has broken the shulker box and placed it back
        if (t != null && t.contains("Items")) {
            nbt.putList("Items", t.getList("Items"))
        }

        // This code gets executed when the player has copied the shulker box in creative mode
        if (item.hasCustomBlockData()) {
            val customData = item.customBlockData!!.tags
            for ((key, value) in customData) {
                nbt.put(key, value)
            }
        }

        return BlockEntityHolder.setBlockAndCreateEntity(this, direct = true, update = true, initialData = nbt) != null
    }

    override fun canHarvestWithHand(): Boolean {
        return false
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

        val box = getOrCreateBlockEntity()
        val block = this.getSide(fromIndex(box.namedTag.getByte("facing").toInt()))
        if ((block !is BlockAir) && (block !is BlockLiquid) && (block !is BlockFlowable)) {
            return false
        }

        player.addWindow(box.inventory)
        return true
    }

    override fun hasComparatorInputOverride(): Boolean {
        return true
    }

    override val comparatorInputOverride: Int
        get() {
            val be = blockEntity ?: return 0

            return calculateRedstone(be.inventory)
        }

    override fun breaksWhenMoved(): Boolean {
        return true
    }

    override fun sticksToPiston(): Boolean {
        return false
    }

    override val isSolid: Boolean
        get() = false

    override fun isSolid(side: BlockFace): Boolean {
        return false
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.UNDYED_SHULKER_BOX, setOf(BlockTags.PNX_SHULKERBOX))
    }
}
