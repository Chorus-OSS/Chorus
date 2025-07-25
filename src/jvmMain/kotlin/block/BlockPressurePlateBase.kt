package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.IntPropertyType
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.Event
import org.chorus_oss.chorus.event.block.BlockRedstoneEvent
import org.chorus_oss.chorus.event.entity.EntityInteractEvent
import org.chorus_oss.chorus.event.player.PlayerInteractEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool.Companion.getBestTool
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.SimpleAxisAlignedBB
import org.chorus_oss.chorus.utils.RedstoneComponent
import org.chorus_oss.chorus.utils.RedstoneComponent.Companion.updateAroundRedstone

abstract class BlockPressurePlateBase(blockState: BlockState) : BlockFlowable(blockState),
    RedstoneComponent {
    @JvmField
    protected var onPitch: Float = 0f

    @JvmField
    protected var offPitch: Float = 0f

    protected abstract fun computeRedstoneStrength(): Int

    override fun canPassThrough(): Boolean {
        return true
    }

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override var minX: Double
        get() = position.x + 0.625
        set(minX) {
            super.minX = minX
        }

    override var minZ: Double
        get() = position.z + 0.625
        set(minZ) {
            super.minZ = minZ
        }

    override var minY: Double
        get() = position.y + 0
        set(minY) {
            super.minY = minY
        }

    override var maxX: Double
        get() = position.x + 0.9375
        set(maxX) {
            super.maxX = maxX
        }

    override var maxZ: Double
        get() = position.z + 0.9375
        set(maxZ) {
            super.maxZ = maxZ
        }

    override var maxY: Double
        get() = if (isActivated) position.y + 0.03125 else position.y + 0.0625
        set(maxY) {
            super.maxY = maxY
        }

    override val isPowerSource: Boolean
        get() = true

    val isActivated: Boolean
        get() = redstonePower == 0

    override val waterloggingLevel: Int
        get() = 1

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (!isSupportValid(down(), BlockFace.UP)) {
                level.useBreakOn(this.position, getBestTool(toolType))
            }
        } else if (type == Level.BLOCK_UPDATE_SCHEDULED) {
            val power = this.redstonePower

            if (power > 0) {
                this.updateState(power)
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
        if (!isSupportValid(block.down(), BlockFace.UP)) {
            return false
        }

        level.setBlock(block.position, this, true, true)
        return true
    }

    override fun recalculateCollisionBoundingBox(): AxisAlignedBB? {
        return SimpleAxisAlignedBB(
            position.x + 0.125,
            position.y,
            position.z + 0.125,
            position.x + 0.875, position.y + 0.25, position.z + 0.875
        )
    }

    override fun onEntityCollide(entity: Entity) {
        if (!Server.instance.settings.levelSettings.enableRedstone) {
            return
        }

        val power = redstonePower

        if (power == 0) {
            val ev: Event = if (entity is Player) {
                PlayerInteractEvent(
                    entity,
                    null,
                    position,
                    null,
                    PlayerInteractEvent.Action.PHYSICAL
                )
            } else {
                EntityInteractEvent(entity, this)
            }

            Server.instance.pluginManager.callEvent(ev)

            if (!ev.cancelled) {
                updateState(power)
            }
        }
    }

    protected fun updateState(oldStrength: Int) {
        val strength = this.computeRedstoneStrength()
        val wasPowered = oldStrength > 0
        val isPowered = strength > 0

        if (oldStrength != strength) {
            this.redstonePower = strength
            level.setBlock(this.position, this, false, false)

            updateAroundRedstone()
            updateAroundRedstone(getSide(BlockFace.DOWN))

            if (!isPowered && wasPowered) {
                this.playOffSound()
                Server.instance.pluginManager.callEvent(BlockRedstoneEvent(this, 15, 0))
            } else if (isPowered && !wasPowered) {
                this.playOnSound()
                Server.instance.pluginManager.callEvent(BlockRedstoneEvent(this, 0, 15))
            }
        }

        if (isPowered) {
            level.scheduleUpdate(this, 20)
        }
    }

    override fun onBreak(item: Item?): Boolean {
        level.setBlock(this.position, get(BlockID.AIR), true, true)

        if (this.redstonePower > 0) {
            updateAroundRedstone()
            updateAroundRedstone(getSide(BlockFace.DOWN))
        }

        return true
    }

    override fun getWeakPower(side: BlockFace): Int {
        return redstonePower
    }

    override fun getStrongPower(side: BlockFace): Int {
        return if (side == BlockFace.UP) redstonePower else 0
    }

    var redstonePower: Int
        get() = getPropertyValue<Int, IntPropertyType>(CommonBlockProperties.REDSTONE_SIGNAL)
        set(power) {
            setPropertyValue<Int, IntPropertyType>(CommonBlockProperties.REDSTONE_SIGNAL, power)
        }

    protected fun playOnSound() {
        level.addLevelSoundEvent(
            position.add(0.5, 0.1, 0.5),
            org.chorus_oss.protocol.packets.LevelSoundEventPacket.Companion.SoundType.PowerOn,
            blockState.blockStateHash()
        )
    }

    protected fun playOffSound() {
        level.addLevelSoundEvent(
            position.add(0.5, 0.1, 0.5),
            org.chorus_oss.protocol.packets.LevelSoundEventPacket.Companion.SoundType.PowerOff,
            blockState.blockStateHash()
        )
    }

    companion object {
        fun isSupportValid(block: Block, blockFace: BlockFace): Boolean {
            return BlockLever.isSupportValid(block, blockFace) || block is BlockFence
        }
    }
}
