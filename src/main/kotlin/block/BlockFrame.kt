package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.BooleanPropertyType
import org.chorus_oss.chorus.block.property.type.IntPropertyType
import org.chorus_oss.chorus.blockentity.BlockEntityID
import org.chorus_oss.chorus.blockentity.BlockEntityItemFrame
import org.chorus_oss.chorus.event.block.ItemFrameUseEvent
import org.chorus_oss.chorus.event.player.PlayerInteractEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockFace.AxisDirection
import org.chorus_oss.chorus.math.BlockFace.Companion.fromIndex
import org.chorus_oss.chorus.math.SimpleAxisAlignedBB
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.protocol.packets.LevelEventPacket
import org.chorus_oss.chorus.utils.Faceable
import java.util.concurrent.ThreadLocalRandom

open class BlockFrame @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockTransparent(blockstate), BlockEntityHolder<BlockEntityItemFrame>, Faceable {
    override var blockFace: BlockFace
        get() = fromIndex(getPropertyValue<Int, IntPropertyType>(CommonBlockProperties.FACING_DIRECTION))
        set(face) {
            setPropertyValue<Int, IntPropertyType>(CommonBlockProperties.FACING_DIRECTION, face.index)
        }

    var isStoringMap: Boolean
        get() = getPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.ITEM_FRAME_MAP_BIT)
        set(map) {
            setPropertyValue<Boolean, BooleanPropertyType>(
                CommonBlockProperties.ITEM_FRAME_MAP_BIT,
                map
            )
        }

    var isStoringPhoto: Boolean
        get() = getPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.ITEM_FRAME_PHOTO_BIT)
        set(hasPhoto) {
            setPropertyValue<Boolean, BooleanPropertyType>(
                CommonBlockProperties.ITEM_FRAME_PHOTO_BIT,
                hasPhoto
            )
        }

    override fun getBlockEntityType(): String = BlockEntityID.ITEM_FRAME

    override fun getBlockEntityClass() = BlockEntityItemFrame::class.java

    override val name: String
        get() = "Item Frame"

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            val support = this.getSideAtLayer(0, facing.getOpposite())
            if (!support.isSolid && support.id != BlockID.COBBLESTONE_WALL) {
                level.useBreakOn(this.position)
                return type
            }
        }

        return 0
    }

    override fun canBeActivated(): Boolean {
        return true
    }

    override val waterloggingLevel: Int
        get() = 1

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
        onUpdate(Level.BLOCK_UPDATE_TOUCH)
        if (player != null && action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) {
            val blockEntity = getOrCreateBlockEntity()
            if (player.isCreative) {
                blockEntity.item = Item.AIR
            } else {
                blockEntity.dropItem(player)
            }
        }
    }

    override fun onActivate(
        item: Item,
        player: Player?,
        blockFace: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float
    ): Boolean {
        if (player != null && player.isSneaking()) return false
        val itemFrame = getOrCreateBlockEntity()
        if (itemFrame.item.isNothing) {
            val itemOnFrame: Item = item.clone()
            val event = ItemFrameUseEvent(player, this, itemFrame, itemOnFrame, ItemFrameUseEvent.Action.PUT)
            Server.instance.pluginManager.callEvent(event)
            if (event.cancelled) return false
            if (player != null && !player.isCreative) {
                itemOnFrame.setCount(itemOnFrame.getCount() - 1)
                player.inventory.setItemInHand(itemOnFrame)
            }
            itemOnFrame.setCount(1)
            itemFrame.item = itemOnFrame
            if (itemOnFrame.id == ItemID.FILLED_MAP) {
                isStoringMap = true
                level.setBlock(this.position, this, true)
            }
            level.addLevelEvent(this.position, LevelEventPacket.SOUND_ITEMFRAME_ITEM_ADD)
        } else {
            val event = ItemFrameUseEvent(player, this, itemFrame, null, ItemFrameUseEvent.Action.ROTATION)
            Server.instance.pluginManager.callEvent(event)
            if (event.cancelled) return false
            itemFrame.itemRotation = (itemFrame.itemRotation + 1) % 8
            if (isStoringMap) {
                isStoringMap = false
                level.setBlock(this.position, this, true)
            }
            level.addLevelEvent(this.position, LevelEventPacket.SOUND_ITEMFRAME_ITEM_ROTATE)
        }
        return true
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
        var target1 = target
        var face1 = face
        if ((!(target1!!.isSolid || target1 is BlockWallBase) && target1 != block || target1 is BlockFrame || (block.isSolid && !block.canBeReplaced()))) {
            return false
        }

        if (target1 == block && block.canBeReplaced()) {
            face1 = BlockFace.UP
            target1 = block.down()
            if (!target1.isSolid && target1 !is BlockWallBase) {
                return false
            }
        }

        blockFace = face1
        isStoringMap = item!!.id == ItemID.FILLED_MAP
        val nbt = CompoundTag()
            .putByte("ItemRotation", 0)
            .putFloat("ItemDropChance", 1.0f)
        if (item.hasCustomBlockData()) {
            for ((key, value) in item.customBlockData!!.entrySet) {
                nbt.put(key, value)
            }
        }
        level.setBlock(block.position, this, true, true)
        val levelBlock = block.levelBlock as BlockFrame?
        var frame = levelBlock!!.blockEntity
        if (frame == null) {
            frame = levelBlock.createBlockEntity(nbt)
        }

        level.addSound(this.position, Sound.BLOCK_ITEMFRAME_PLACE)
        return true
    }

    override fun onBreak(item: Item?): Boolean {
        level.setBlock(this.position, layer, get(BlockID.AIR), true, true)
        level.addLevelEvent(this.position, LevelEventPacket.SOUND_ITEMFRAME_BREAK)
        return true
    }

    override fun getDrops(item: Item): Array<Item> {
        val itemFrame = blockEntity
        return if (itemFrame != null && ThreadLocalRandom.current()
                .nextFloat() <= itemFrame.itemDropChance
        ) {
            arrayOf(
                toItem(), itemFrame.item.clone()
            )
        } else {
            arrayOf(
                toItem()
            )
        }
    }

    override fun canPassThrough(): Boolean {
        return true
    }

    override fun hasComparatorInputOverride(): Boolean {
        return true
    }

    override val comparatorInputOverride: Int
        get() {
            val blockEntity = blockEntity

            if (blockEntity != null) {
                return blockEntity.analogOutput
            }

            return super.comparatorInputOverride
        }

    val facing: BlockFace
        get() = blockFace

    override val hardness: Double
        get() = 0.25

    override fun breaksWhenMoved(): Boolean {
        return true
    }

    override fun sticksToPiston(): Boolean {
        return false
    }

    override fun recalculateBoundingBox(): AxisAlignedBB? {
        val aabb = arrayOf(
            doubleArrayOf(2.0 / 16, 14.0 / 16),
            doubleArrayOf(2.0 / 16, 14.0 / 16),
            doubleArrayOf(2.0 / 16, 14.0 / 16)
        )

        val facing = facing
        if (facing.axisDirection == AxisDirection.POSITIVE) {
            val axis = facing.axis.ordinal
            aabb[axis][0] = 0.0
            aabb[axis][1] = 1.0 / 16
        }

        return SimpleAxisAlignedBB(
            aabb[0][0] + position.x, aabb[1][0] + position.y, aabb[2][0] + position.z,
            aabb[0][1] + position.x, aabb[1][1] + position.y, aabb[2][1] + position.z
        )
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.FRAME,
            CommonBlockProperties.FACING_DIRECTION,
            CommonBlockProperties.ITEM_FRAME_MAP_BIT,
            CommonBlockProperties.ITEM_FRAME_PHOTO_BIT
        )
    }
}