package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.BooleanPropertyType
import org.chorus_oss.chorus.blockentity.BlockEntityDispenser
import org.chorus_oss.chorus.blockentity.BlockEntityEjectable
import org.chorus_oss.chorus.blockentity.BlockEntityID
import org.chorus_oss.chorus.dispenser.DispenseBehavior
import org.chorus_oss.chorus.dispenser.DispenseBehaviorRegister.getBehavior
import org.chorus_oss.chorus.dispenser.DropperDispenseBehavior
import org.chorus_oss.chorus.dispenser.FlintAndSteelDispenseBehavior
import org.chorus_oss.chorus.inventory.ContainerInventory.Companion.calculateRedstone
import org.chorus_oss.chorus.inventory.InventoryHolder
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemBlock
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockFace.Companion.fromIndex
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.nbt.tag.Tag
import org.chorus_oss.chorus.utils.Faceable
import org.chorus_oss.chorus.utils.RedstoneComponent
import org.chorus_oss.protocol.packets.LevelEventPacket
import org.chorus_oss.protocol.types.Vector3f
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.abs

open class BlockDispenser @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockSolid(blockstate), RedstoneComponent, Faceable, BlockEntityHolder<BlockEntityEjectable> {
    override fun hasComparatorInputOverride(): Boolean {
        return true
    }

    override val name: String
        get() = "Dispenser"

    override fun getBlockEntityType(): String {
        return BlockEntityID.DISPENSER
    }

    override val hardness: Double
        get() = 3.5

    override val resistance: Double
        get() = 3.5

    override fun getBlockEntityClass(): Class<out BlockEntityEjectable> {
        return BlockEntityDispenser::class.java
    }

    override fun toItem(): Item {
        return ItemBlock(this.blockState, name, 0)
    }

    override val comparatorInputOverride: Int
        get() {
            val blockEntity: InventoryHolder? = blockEntity

            if (blockEntity != null) {
                return calculateRedstone(blockEntity.inventory)
            }

            return 0
        }

    var isTriggered: Boolean
        get() = getPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.TRIGGERED_BIT)
        set(value) {
            setPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.TRIGGERED_BIT, value)
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
        if (isNotActivate(player)) return false

        val blockEntity = blockEntity ?: return false

        player!!.addWindow(blockEntity.inventory)
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
        if (player != null) {
            if (abs(player.position.x - position.x) < 2 && abs(player.position.z - position.z) < 2) {
                val y = player.position.y + player.getEyeHeight()

                blockFace = if (y - position.y > 2) {
                    BlockFace.UP
                } else if (position.y - y > 0) {
                    BlockFace.DOWN
                } else {
                    player.getHorizontalFacing().getOpposite()
                }
            } else {
                blockFace = player.getHorizontalFacing().getOpposite()
            }
        }

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

    override fun onUpdate(type: Int): Int {
        if (!Server.instance.settings.levelSettings.enableRedstone) {
            return 0
        }

        if (type == Level.BLOCK_UPDATE_SCHEDULED) {
            this.dispense()

            return type
        } else if (type == Level.BLOCK_UPDATE_REDSTONE || type == Level.BLOCK_UPDATE_NORMAL) {
            val triggered = this.isTriggered

            if (this.isGettingPower && !triggered) {
                this.isTriggered = true
                level.setBlock(this.position, this, direct = false, update = false)
                level.scheduleUpdate(this, this.position, 4)
            } else if (!this.isGettingPower && triggered) {
                this.isTriggered = false
                level.setBlock(this.position, this, direct = false, update = false)
            }

            return type
        }

        return 0
    }

    open fun dispense() {
        val blockEntity = blockEntity ?: return

        val rand: Random = ThreadLocalRandom.current()
        var r = 1
        var slot = -1
        var target: Item? = null

        val inv = blockEntity.inventory
        for ((key, item) in inv.contents.entries) {
            if (!item.isNothing && rand.nextInt(r++) == 0) {
                target = item
                slot = key
            }
        }

        val facing = blockFace

        if (target == null) {
            level.addSound(this.position, Sound.RANDOM_CLICK, 1.0f, 1.2f)
            blockEntity.setDirty()
            return
        } else {
            if (getDispenseBehavior(target) !is DropperDispenseBehavior && getDispenseBehavior(target) !is FlintAndSteelDispenseBehavior) level.addSound(
                this.position, Sound.RANDOM_CLICK, 1.0f, 1.0f
            )
        }

        val pk = LevelEventPacket(
            eventType = LevelEventPacket.PARTICLE_SHOOT,
            position = Vector3f(
                x = 0.5f + facing.xOffset * 0.7f,
                y = 0.5f + facing.yOffset * 0.7f,
                z = 0.5f + facing.zOffset * 0.7f,
            ),
            eventData = 7
        )
        level.addChunkPacket(position.chunkX, position.chunkZ, pk)

        val origin: Item = target
        target = target.clone()

        val behavior = getDispenseBehavior(target)
        val result = behavior.dispense(this, facing, target)

        target.count--
        inv.setItem(slot, target)

        if (result != null) {
            if (result.id == origin.id || result.damage != origin.damage) {
                val fit = inv.addItem(result)

                if (fit.size > 0) {
                    for (drop in fit) {
                        level.dropItem(this.position, drop)
                    }
                }
            } else {
                inv.setItem(slot, result)
            }
        }
    }

    protected open fun getDispenseBehavior(item: Item): DispenseBehavior {
        return getBehavior(item.id)
    }

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override val toolTier: Int
        get() = ItemTool.TIER_WOODEN

    val dispensePosition: Vector3
        get() {
            val facing = blockFace
            return position.add(
                0.5 + 0.7 * facing.xOffset,
                0.5 + 0.7 * facing.yOffset,
                0.5 + 0.7 * facing.zOffset
            )
        }

    override var blockFace: BlockFace
        get() = fromIndex(getPropertyValue(CommonBlockProperties.FACING_DIRECTION))
        set(face) {
            setPropertyValue(CommonBlockProperties.FACING_DIRECTION, face.index)
        }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(
                BlockID.DISPENSER,
                CommonBlockProperties.FACING_DIRECTION,
                CommonBlockProperties.TRIGGERED_BIT
            )
    }
}
