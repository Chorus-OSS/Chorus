package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.BooleanPropertyType
import org.chorus_oss.chorus.blockentity.BlockEntityFlowerPot
import org.chorus_oss.chorus.blockentity.BlockEntityID
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.Item.Companion.get
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.nbt.tag.CompoundTag

class BlockFlowerPot(blockState: BlockState = properties.defaultState) : BlockFlowable(blockState),
    BlockEntityHolder<BlockEntityFlowerPot> {
    override val waterloggingLevel: Int
        get() = 1

    override val name: String
        get() = "Flower Pot"

    override fun getBlockEntityClass() = BlockEntityFlowerPot::class.java

    override fun getBlockEntityType(): String {
        return BlockEntityID.FLOWER_POT
    }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (!BlockLever.isSupportValid(down(), BlockFace.UP)) {
                level.useBreakOn(this.position)
                return type
            }
        }
        return 0
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
        if (!BlockLever.isSupportValid(block.down(), BlockFace.UP)) {
            return false
        }

        val nbt = CompoundTag()
        if (item!!.hasCustomBlockData()) {
            for ((key, value) in item.customBlockData!!.entrySet) {
                nbt.put(key, value)
            }
        }

        return BlockEntityHolder.setBlockAndCreateEntity(this, true, true, nbt) != null
    }

    val flower: Item
        get() {
            val blockEntity = blockEntity
            if (blockEntity == null || !blockEntity.namedTag.containsCompound("PlantBlock")) {
                return Item.AIR
            }
            val plantBlockTag = blockEntity.namedTag.getCompound("PlantBlock")
            // TODO: Vanilla uses a Block NBT, not an Item
            val id = plantBlockTag.getString("itemId")
            val meta = plantBlockTag.getInt("itemMeta")
            return get(id, meta)
        }

    fun setFlower(item: Item?): Boolean {
        if (item == null || item.isNothing) {
            removeFlower()
            return true
        }
        val potBlock = item.getSafeBlockState().toBlock()
        if (potBlock is FlowerPotBlock) {
            val blockEntity = getOrCreateBlockEntity()
            blockEntity.namedTag.putCompound("PlantBlock", potBlock.plantBlockTag)

            setPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.UPDATE_BIT, true)
            level.setBlock(this.position, this, true)
            blockEntity.spawnToAll()
            return true
        }

        return false
    }

    fun removeFlower() {
        val blockEntity = getOrCreateBlockEntity()
        blockEntity.namedTag.remove("PlantBlock")

        setPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.UPDATE_BIT, false)
        level.setBlock(this.position, this, true)
        blockEntity.spawnToAll()
    }

    fun hasFlower(): Boolean {
        val blockEntity = blockEntity ?: return false
        return blockEntity.namedTag.containsCompound("PlantBlock")
    }

    override fun canBeActivated(): Boolean {
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
        if (getPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.UPDATE_BIT)) {
            if (player == null) {
                return false
            }

            if (!item.isNothing) return false

            if (hasFlower()) {
                val flower = flower
                removeFlower()
                player.giveItem(flower)
                return true
            }
        }

        if (item.isNothing) {
            return false
        }

        getOrCreateBlockEntity()
        if (hasFlower()) {
            return false
        }

        if (!setFlower(item)) {
            return false
        }

        if (player == null || !player.isCreative) {
            item.count--
        }
        return true
    }

    override fun getDrops(item: Item): Array<Item> {
        var dropInside = false
        var insideID: String? = "minecraft:air"
        var insideMeta = 0
        val blockEntity = blockEntity
        if (blockEntity != null) {
            dropInside = true
            insideID = blockEntity.namedTag.getCompound("PlantBlock").getString("itemId")
            insideMeta = blockEntity.namedTag.getCompound("PlantBlock").getInt("itemMeta")
        }
        return if (dropInside) {
            arrayOf(
                toItem(),
                get(insideID!!, insideMeta)
            )
        } else {
            arrayOf(
                toItem()
            )
        }
    }

    override fun recalculateBoundingBox(): AxisAlignedBB {
        return this
    }

    override var minX: Double
        get() = position.x + 0.3125
        set(minX) {
            super.minX = minX
        }

    override var minZ: Double
        get() = position.z + 0.3125
        set(minZ) {
            super.minZ = minZ
        }

    override var maxX: Double
        get() = position.x + 0.6875
        set(maxX) {
            super.maxX = maxX
        }

    override var maxY: Double
        get() = position.y + 0.375
        set(maxY) {
            super.maxY = maxY
        }

    override var maxZ: Double
        get() = position.z + 0.6875
        set(maxZ) {
            super.maxZ = maxZ
        }

    override fun canPassThrough(): Boolean {
        return false
    }

    /**
     * 实现了此接口的方块可以放入花盆中
     */
    interface FlowerPotBlock {
        val plantBlockTag: CompoundTag
            /**
             * 获取方块在花盆NBT中的标签
             *
             *
             * 形如以下形式：
             *
             *
             * `"PlantBlock": {
             * "name": "minecraft:red_flower",
             * "states": {
             * "flower_type": "poppy"
             * },
             * "version": 17959425i
             * "itemId": xxx,
             * "itemMeta": xxx
             * }
            ` *
             *
             * 请注意，必须在这个tag中包含键"itemId"与"itemMeta"。服务端将通过读取这两个参数快速重建Item对象，而不是通过stateId重建。这太慢了
             *
             * @return 方块在花盆NBT中的标签
             */
            get() {
                val block = this as Block
                val tag = block.blockState.blockStateTag.copy()
                val item = block.toItem()
                return tag.putString("itemId", item.id)
                    .putInt("itemMeta", item.damage) //only exist in PNX
            }
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.FLOWER_POT, CommonBlockProperties.UPDATE_BIT)
    }
}

