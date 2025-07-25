package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.blockentity.BlockEntityChiseledBookshelf
import org.chorus_oss.chorus.blockentity.BlockEntityID
import org.chorus_oss.chorus.event.player.PlayerInteractEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemBook
import org.chorus_oss.chorus.item.ItemBookWritable
import org.chorus_oss.chorus.item.ItemEnchantedBook
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockFace.Companion.fromHorizontalIndex
import org.chorus_oss.chorus.math.Vector2
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.utils.Faceable

class BlockChiseledBookshelf @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockBookshelf(blockState), BlockEntityHolder<BlockEntityChiseledBookshelf>, Faceable {
    override val name: String
        get() = "Chiseled Bookshelf"

    override fun getDrops(item: Item): Array<Item> {
        val blockEntity: BlockEntityChiseledBookshelf? = this.blockEntity
        if (blockEntity != null) {
            return blockEntity.items
        }
        return Item.EMPTY_ARRAY
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
        blockFace = player?.getHorizontalFacing()?.getOpposite() ?: BlockFace.SOUTH
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

    override fun getBlockEntityClass(): Class<out BlockEntityChiseledBookshelf> {
        return BlockEntityChiseledBookshelf::class.java
    }

    override fun getBlockEntityType(): String {
        return BlockEntityID.CHISELED_BOOKSHELF
    }

    override fun onTouch(
        vector: Vector3,
        item: Item,
        face: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float,
        player: Player?,
        action: PlayerInteractEvent.Action
    ) {
        if (action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            val blockFace = blockFace
            checkNotNull(player)
            if (player.getHorizontalFacing().getOpposite() == blockFace) {
                /*
                 * south z==1  The lower left corner is the origin
                 * east  x==1  The lower right corner is the origin
                 * west  x==0  The lower left corner is the origin
                 * north z==0  The lower right corner is the origin
                 */
                val clickPos = when (blockFace) {
                    BlockFace.NORTH -> Vector2((1 - fx).toDouble(), fy.toDouble())
                    BlockFace.SOUTH -> Vector2(fx.toDouble(), fy.toDouble())
                    BlockFace.WEST -> Vector2(fz.toDouble(), fy.toDouble())
                    BlockFace.EAST -> Vector2((1 - fz).toDouble(), fy.toDouble())
                    else -> throw IllegalArgumentException(blockFace.toString())
                }
                val index = getRegion(clickPos)
                val blockEntity: BlockEntityChiseledBookshelf? = this.blockEntity
                if (blockEntity != null) {
                    if (blockEntity.hasBook(index)) {
                        val book: Item = blockEntity.removeBook(index)
                        player.inventory.addItem(book)
                    } else if (item is ItemBook || item is ItemEnchantedBook || item is ItemBookWritable) {
                        val itemClone: Item = item.clone()
                        if (!player.isCreative) {
                            itemClone.setCount(itemClone.getCount() - 1)
                            player.inventory.setItemInHand(itemClone)
                        }
                        itemClone.setCount(1)
                        blockEntity.setBook(itemClone, index)
                    }
                    this.setPropertyValue(
                        CommonBlockProperties.BOOKS_STORED,
                        blockEntity.booksStoredBit
                    )
                    level.setBlock(this.position, this, true)
                }
            }
        }
    }

    override var blockFace: BlockFace
        get() = fromHorizontalIndex(getPropertyValue(CommonBlockProperties.DIRECTION))
        set(face) {
            setPropertyValue(CommonBlockProperties.DIRECTION, face.horizontalIndex)
        }

    private fun getRegion(clickPos: Vector2): Int {
        return if (clickPos.x - 0.333333 < 0) {
            if (clickPos.y - 0.5 < 0) 3 else 0
        } else if (clickPos.x - 0.666666 < 0) {
            if (clickPos.y - 0.5 < 0) 4 else 1
        } else {
            if (clickPos.y - 0.5 < 0) 5 else 2
        }
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(
                BlockID.CHISELED_BOOKSHELF,
                CommonBlockProperties.BOOKS_STORED,
                CommonBlockProperties.DIRECTION
            )
    }
}