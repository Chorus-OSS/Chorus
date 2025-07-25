package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.CommonPropertyMap
import org.chorus_oss.chorus.block.property.enums.MinecraftCardinalDirection
import org.chorus_oss.chorus.blockentity.BlockEntityChest
import org.chorus_oss.chorus.blockentity.BlockEntityID
import org.chorus_oss.chorus.inventory.ContainerInventory.Companion.calculateRedstone
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemBlock
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockFace.Companion.fromHorizontalIndex
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.nbt.tag.StringTag
import org.chorus_oss.chorus.nbt.tag.Tag
import org.chorus_oss.chorus.utils.Faceable

open class BlockChest @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockTransparent(blockState), Faceable, BlockEntityHolder<BlockEntityChest> {
    override fun getBlockEntityClass(): Class<out BlockEntityChest> {
        return BlockEntityChest::class.java
    }

    override fun getBlockEntityType(): String {
        return BlockEntityID.CHEST
    }

    override fun canBeActivated(): Boolean {
        return true
    }

    override val name: String
        get() = "Chest"

    override val hardness: Double
        get() = 2.5

    override val waterloggingLevel: Int
        get() = 1

    override val resistance: Double
        get() = 12.5

    override val toolType: Int
        get() = ItemTool.TYPE_AXE

    override var minX: Double
        get() = position.x + 0.0625
        set(minX) {
            super.minX = minX
        }

    override var minY: Double
        get() = position.y
        set(minY) {
            super.minY = minY
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

        BlockEntityHolder.setBlockAndCreateEntity(this, true, update = true, initialData = nbt)
            ?: return false

        tryPair()

        return true
    }

    /**
     * 尝试与旁边箱子连接
     *
     *
     * Try to pair with a chest next to it
     *
     * @return 是否连接成功 <br></br> Whether pairing was successful
     */
    protected fun tryPair(): Boolean {
        val blockEntity: BlockEntityChest = blockEntity ?: return false

        val chest: BlockEntityChest = findPair() ?: return false

        chest.pairWith(blockEntity)
        blockEntity.pairWith(chest)
        return true
    }

    /**
     * 寻找附近的可配对箱子
     *
     *
     * Search for nearby chest to pair with
     *
     * @return 找到的可配对箱子。若没找到，则为null <br></br> Chest to pair with. Null if none have been found
     */
    protected fun findPair(): BlockEntityChest? {
        val universe: List<MinecraftCardinalDirection> =
            CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION.validValues
        val thisFace = blockFace
        for (face in universe) {
            val side = this.getSide(CommonPropertyMap.CARDINAL_BLOCKFACE[face]!!)
            if (side is BlockChest) {
                val pairFace = side.blockFace
                if (thisFace == pairFace) {
                    return side.blockEntity
                }
            }
        }
        return null
    }

    override fun cloneTo(pos: Locator): Boolean {
        if (!super.cloneTo(pos)) return false
        else {
            val blockEntity: BlockEntityChest? = this.blockEntity
            if (blockEntity != null && blockEntity.isPaired) (pos.levelBlock as BlockChest).tryPair()
            return true
        }
    }

    override fun onBreak(item: Item?): Boolean {
        blockEntity?.unpair()
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
        if (isNotActivate(player)) return false
        if (player != null) {
            val itemInHand = player.inventory.itemInHand
            if (player.isSneaking() && !(itemInHand.isTool || itemInHand.isNothing)) return false
        }

        val top = up()
        if (!top.isTransparent) {
            return false
        }

        val chest: BlockEntityChest = getOrCreateBlockEntity()
        if (chest.namedTag.contains("Lock") && chest.namedTag.get("Lock") is StringTag
            && (chest.namedTag.getString("Lock") != item.customName)
        ) {
            return false
        }

        player?.addWindow(chest.inventory)
        return true
    }

    override fun hasComparatorInputOverride(): Boolean {
        return true
    }

    override val comparatorInputOverride: Int
        get() {
            val blockEntity: BlockEntityChest? = blockEntity

            if (blockEntity != null) {
                return calculateRedstone(blockEntity.inventory)
            }

            return super.comparatorInputOverride
        }

    override fun toItem(): Item {
        return ItemBlock(properties.defaultState, name, 0)
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

    override fun canBePushed(): Boolean {
        return canMove()
    }

    override fun canBePulled(): Boolean {
        return canMove()
    }

    /**
     * TODO: Double chests cannot be moved
     */
    protected fun canMove(): Boolean {
        val blockEntity: BlockEntityChest? = this.blockEntity
        return blockEntity == null || !blockEntity.isPaired
    }

    override fun getDrops(item: Item): Array<Item> {
        return arrayOf(ItemBlock(properties.defaultState, name, 0))
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.CHEST, CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION)
    }
}
