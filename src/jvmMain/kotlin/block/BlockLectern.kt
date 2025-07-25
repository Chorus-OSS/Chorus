package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.CommonPropertyMap
import org.chorus_oss.chorus.block.property.type.BooleanPropertyType
import org.chorus_oss.chorus.blockentity.BlockEntityID
import org.chorus_oss.chorus.blockentity.BlockEntityLectern
import org.chorus_oss.chorus.event.block.BlockRedstoneEvent
import org.chorus_oss.chorus.event.block.LecternDropBookEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.utils.Faceable
import org.chorus_oss.chorus.utils.RedstoneComponent
import org.chorus_oss.chorus.utils.RedstoneComponent.Companion.updateAroundRedstone

class BlockLectern @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockTransparent(blockstate), RedstoneComponent, Faceable, BlockEntityHolder<BlockEntityLectern> {
    override val name: String
        get() = "Lectern"

    override fun getBlockEntityClass() = BlockEntityLectern::class.java

    override fun getBlockEntityType(): String {
        return BlockEntityID.LECTERN
    }

    override val waterloggingLevel: Int
        get() = 1

    override fun canBeActivated(): Boolean {
        return true
    }

    override val hardness: Double
        get() = 2.5

    override val resistance: Double
        get() = 12.5

    override val toolType: Int
        get() = ItemTool.TYPE_AXE

    override var maxY: Double
        get() = position.y + 0.89999
        set(maxY) {
            super.maxY = maxY
        }

    override fun hasComparatorInputOverride(): Boolean {
        return true
    }

    override val comparatorInputOverride: Int
        get() {
            var power = 0
            val page: Int
            val maxPage: Int
            val lectern = blockEntity
            if (lectern != null && lectern.hasBook()) {
                maxPage = lectern.totalPages
                page = lectern.leftPage + 1
                power = ((page.toFloat() / maxPage) * 16).toInt()
            }
            return power
        }

    override var blockFace: BlockFace
        get() = CommonPropertyMap.CARDINAL_BLOCKFACE[getPropertyValue(
            CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION
        )]!!
        set(face) {
            setPropertyValue(
                CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION,
                CommonPropertyMap.CARDINAL_BLOCKFACE.inverse()[face]!!
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
        blockFace = player?.getDirection()?.getOpposite() ?: BlockFace.SOUTH
        return BlockEntityHolder.setBlockAndCreateEntity(this) != null
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
        val lectern = getOrCreateBlockEntity()
        val currentBook = lectern.book
        if (!currentBook.isNothing) {
            return true
        }
        if (item.id != ItemID.WRITTEN_BOOK && item.id != ItemID.WRITABLE_BOOK) {
            return false
        }

        if (player == null || !player.isCreative) {
            item.count--
        }

        val newBook: Item = item.clone()
        newBook.setCount(1)
        lectern.book = newBook
        lectern.spawnToAll()
        level.addSound(position.add(0.5, 0.5, 0.5), Sound.ITEM_BOOK_PUT)
        return true
    }

    override val isPowerSource: Boolean
        get() = true

    var isActivated: Boolean
        get() = getPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.POWERED_BIT)
        set(activated) {
            setPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.POWERED_BIT, activated)
        }

    fun executeRedstonePulse() {
        if (isActivated) {
            level.cancelSheduledUpdate(this.position, this)
        } else {
            Server.instance.pluginManager.callEvent(BlockRedstoneEvent(this, 0, 15))
        }

        level.scheduleUpdate(this, this.position, 4)
        isActivated = true
        level.setBlock(this.position, this, true, false)
        level.addSound(position.add(0.5, 0.5, 0.5), Sound.ITEM_BOOK_PAGE_TURN)

        updateAroundRedstone()
        updateAroundRedstone(getSide(BlockFace.DOWN), BlockFace.UP)
    }

    override fun getWeakPower(face: BlockFace): Int {
        return if (isActivated) 15 else 0
    }

    override fun getStrongPower(side: BlockFace): Int {
        return if (side == BlockFace.DOWN) this.getWeakPower(side) else 0
    }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_SCHEDULED) {
            if (isActivated) {
                Server.instance.pluginManager.callEvent(BlockRedstoneEvent(this, 15, 0))

                isActivated = false
                level.setBlock(this.position, this, true, false)
                updateAroundRedstone()
                updateAroundRedstone(getSide(BlockFace.DOWN), BlockFace.UP)
            }

            return Level.BLOCK_UPDATE_SCHEDULED
        }

        return 0
    }

    fun dropBook(player: Player) {
        val lectern = blockEntity ?: return

        val book = lectern.book
        if (book.isNothing) {
            return
        }

        val dropBookEvent = LecternDropBookEvent(player, lectern, book)
        Server.instance.pluginManager.callEvent(dropBookEvent)
        if (dropBookEvent.cancelled) {
            return
        }

        lectern.book = Item.AIR
        lectern.spawnToAll()
        level.dropItem(lectern.position.add(0.5, 0.6, 0.5), dropBookEvent.getBook())
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.LECTERN,
            CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION,
            CommonBlockProperties.POWERED_BIT
        )
    }
}
