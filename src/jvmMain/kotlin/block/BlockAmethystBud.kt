package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.utils.Faceable
import java.util.*

abstract class BlockAmethystBud(blockState: BlockState) : BlockTransparent(blockState), Faceable {
    override val name: String
        get() = "$namePrefix Amethyst Bud"

    protected abstract val namePrefix: String

    override val waterloggingLevel: Int
        get() = 1

    override val hardness: Double
        get() = 1.5

    override val resistance: Double
        get() = 1.5

    abstract override val lightLevel: Int

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override val toolTier: Int
        get() = ItemTool.TIER_IRON

    override fun getDrops(item: Item): Array<Item> {
        return Item.EMPTY_ARRAY
    }

    override val isSolid: Boolean
        get() = false

    override var blockFace: BlockFace
        get() = getPropertyValue(CommonBlockProperties.MINECRAFT_BLOCK_FACE)
        set(face) {
            setPropertyValue(
                CommonBlockProperties.MINECRAFT_BLOCK_FACE,
                face
            )
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
        if (!target!!.isSolid) {
            return false
        }

        blockFace = face
        level.setBlock(block.position, this, direct = true, update = true)
        return true
    }

    override fun onBreak(item: Item?): Boolean {
        if (item != null) {
            if (item.isPickaxe) {
                Arrays.stream(this.getDrops(item)).forEach { item1 ->
                    if (item1 != null) {
                        level.dropItem(
                            position.add(0.5, 0.0, 0.5), item1
                        )
                    }
                }
                level.setBlock(this.position, layer, get(BlockID.AIR), direct = true, update = true)
            } else {
                level.setBlock(this.position, layer, get(BlockID.AIR), direct = true, update = true)
            }
        }

        return true
    }

    override fun onUpdate(type: Int): Int {
        if ((getSide(blockFace.getOpposite()).isAir)) {
            this.onBreak(Item.get(ItemID.DIAMOND_PICKAXE))
        }

        return 0
    }
}
