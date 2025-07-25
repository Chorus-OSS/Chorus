package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.blockentity.BlockEntityBeehive
import org.chorus_oss.chorus.blockentity.BlockEntityID
import org.chorus_oss.chorus.event.player.PlayerInteractEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.vibration.VibrationEvent
import org.chorus_oss.chorus.level.vibration.VibrationType
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockFace.Companion.fromHorizontalIndex
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.utils.Faceable

open class BlockBeehive @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockSolid(blockstate), Faceable, BlockEntityHolder<BlockEntityBeehive> {
    override val name: String
        get() = "Beehive"

    override fun getBlockEntityType(): String {
        return BlockEntityID.BEEHIVE
    }

    override fun getBlockEntityClass(): Class<out BlockEntityBeehive> {
        return BlockEntityBeehive::class.java
    }

    override val toolType: Int
        get() = ItemTool.TYPE_AXE

    override val burnChance: Int
        get() = 5

    override val burnAbility: Int
        get() = 20

    override val hardness: Double
        get() = 0.6

    override val resistance: Double
        get() = 3.0

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
        blockFace = if (player == null) {
            BlockFace.SOUTH
        } else {
            player.getDirection().getOpposite()
        }

        val honeyLevel = (if (item!!.hasCustomBlockData()) item.customBlockData!!.getByte("HoneyLevel") else 0).toInt()
        this.honeyLevel = honeyLevel
        val beehive = BlockEntityHolder.setBlockAndCreateEntity(
            this,
            direct = true,
            update = true,
            initialData = item.customBlockData
        )
            ?: return false

        if (beehive.namedTag.getByte("ShouldSpawnBees") > 0) {
            val validSpawnFaces = beehive.scanValidSpawnFaces(true)
            for (occupant in beehive.getOccupants()) {
                beehive.spawnOccupant(occupant, validSpawnFaces)
            }

            beehive.namedTag.putByte("ShouldSpawnBees", 0)
        }
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
        if (item.id == ItemID.SHEARS && isFull) {
            if (player != null) {
                honeyCollected(player)
            }
            level.addSound(position.add(0.5, 0.5, 0.5), Sound.BLOCK_BEEHIVE_SHEAR)
            item.useOn(this)
            for (i in 0..2) {
                level.dropItem(this.position, Item.get(ItemID.HONEYCOMB))
            }
            level.vibrationManager.callVibrationEvent(
                VibrationEvent(
                    this,
                    position.add(0.5, 0.5, 0.5), VibrationType.SHEAR
                )
            )
            return true
        }
        return false
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
        if (player != null) getOrCreateBlockEntity().interactingEntity = player
        super.onTouch(vector, item, face, fx, fy, fz, player, action)
    }

    override fun canBeActivated(): Boolean {
        return true
    }

    @JvmOverloads
    fun honeyCollected(player: Player, angerBees: Boolean = Server.instance.getDifficulty() > 0 && !player.isCreative) {
        getOrCreateBlockEntity().honeyLevel = 0
        if (down().id != BlockID.CAMPFIRE && angerBees) {
            angerBees(player)
        }
    }

    fun angerBees(player: Player?) {
        val beehive = blockEntity
        beehive?.angerBees(player)
    }

    override fun toItem(): Item {
        val item: Item = super.toItem()
        val beehive = blockEntity
        if (beehive != null) {
            beehive.saveNBT()
            if (!beehive.isHoneyEmpty || !beehive.isEmpty) {
                val copy = beehive.namedTag.copy()
                copy.putByte("HoneyLevel", honeyLevel)
                item.setCustomBlockData(copy)
            }
        }
        return item
    }

    override fun canSilkTouch(): Boolean {
        return true
    }

    override fun canHarvestWithHand(): Boolean {
        return true
    }

    override fun getDrops(item: Item): Array<Item> {
        return Item.EMPTY_ARRAY
    }

    override var blockFace: BlockFace
        get() = fromHorizontalIndex(getPropertyValue(CommonBlockProperties.DIRECTION))
        set(face) {
            setPropertyValue(CommonBlockProperties.DIRECTION, face.horizontalIndex)
        }

    var honeyLevel: Int
        get() = getPropertyValue(CommonBlockProperties.HONEY_LEVEL)
        set(honeyLevel) {
            setPropertyValue(CommonBlockProperties.HONEY_LEVEL, honeyLevel)
        }

    val isEmpty: Boolean
        get() = honeyLevel == CommonBlockProperties.HONEY_LEVEL.min

    val isFull: Boolean
        get() = getPropertyValue(CommonBlockProperties.HONEY_LEVEL) == CommonBlockProperties.HONEY_LEVEL.max

    override fun hasComparatorInputOverride(): Boolean {
        return true
    }

    override val comparatorInputOverride: Int
        get() = honeyLevel

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.BEEHIVE, CommonBlockProperties.DIRECTION, CommonBlockProperties.HONEY_LEVEL)
    }
}
