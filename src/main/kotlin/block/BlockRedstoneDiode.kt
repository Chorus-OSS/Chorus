package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.BlockLever.Companion.isSupportValid
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.CommonPropertyMap
import org.chorus_oss.chorus.event.redstone.RedstoneUpdateEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.SimpleAxisAlignedBB
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.utils.Faceable
import org.chorus_oss.chorus.utils.RedstoneComponent
import kotlin.math.max

abstract class BlockRedstoneDiode(blockstate: BlockState) : BlockFlowable(blockstate),
    RedstoneComponent, Faceable {
    @JvmField
    protected var isPowered: Boolean = false

    override val waterloggingLevel: Int
        get() = 2

    override fun canBeFlowedInto(): Boolean {
        return false
    }

    override fun onBreak(item: Item?): Boolean {
        level.setBlock(this.position, get(BlockID.AIR), true, true)

        if (Server.instance.settings.levelSettings.enableRedstone) {
            updateAllAroundRedstone()
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
        if (!isSupportValid(block.down())) {
            return false
        }

        blockFace = if (player != null) player.getDirection().getOpposite() else BlockFace.SOUTH
        if (!level.setBlock(block.position, this, true, true)) {
            return false
        }

        if (Server.instance.settings.levelSettings.enableRedstone) {
            if (shouldBePowered()) {
                level.scheduleUpdate(this, 1)
            }
        }
        return true
    }

    protected fun isSupportValid(support: Block): Boolean {
        return isSupportValid(support, BlockFace.UP) || support is BlockCauldron
    }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_SCHEDULED) {
            if (!Server.instance.settings.levelSettings.enableRedstone) {
                return 0
            }

            if (!this.isLocked) {
                val pos = this.position
                val shouldBePowered = this.shouldBePowered()

                if (this.isPowered && !shouldBePowered) {
                    level.setBlock(pos, this.unpowered, true, true)

                    val side = this.getSide(facing!!.getOpposite())
                    side.onUpdate(Level.BLOCK_UPDATE_REDSTONE)
                    RedstoneComponent.updateAroundRedstone(side)
                } else if (!this.isPowered) {
                    level.setBlock(pos, this.getPowered(), true, true)
                    val side = this.getSide(facing!!.getOpposite())
                    side.onUpdate(Level.BLOCK_UPDATE_REDSTONE)
                    RedstoneComponent.updateAroundRedstone(side)

                    if (!shouldBePowered) {
                        level.scheduleUpdate(getPowered(), this.position, this.delay)
                    }
                }
            }
        } else if (type == Level.BLOCK_UPDATE_NORMAL || type == Level.BLOCK_UPDATE_REDSTONE) {
            if (type == Level.BLOCK_UPDATE_NORMAL && !isSupportValid(down())) {
                level.useBreakOn(this.position)
                return Level.BLOCK_UPDATE_NORMAL
            } else if (Server.instance.settings.levelSettings.enableRedstone) {
                // Redstone event
                val ev = RedstoneUpdateEvent(this)
                Server.instance.pluginManager.callEvent(ev)
                if (ev.cancelled) {
                    return 0
                }

                this.updateState()
                return type
            }
        }
        return 0
    }

    open fun updateState() {
        if (!this.isLocked) {
            val shouldPowered = this.shouldBePowered()

            if ((this.isPowered && !shouldPowered || !this.isPowered && shouldPowered) && !level.isBlockTickPending(
                    this.position,
                    this
                )
            ) {
                /*int priority = -1;

                if (this.isFacingTowardsRepeater()) {
                    priority = -3;
                } else if (this.isPowered) {
                    priority = -2;
                }*/

                level.scheduleUpdate(
                    this, this.position,
                    delay
                )
            }
        }
    }

    open val isLocked: Boolean
        get() = false

    protected open fun calculateInputStrength(): Int {
        val face = facing
        val pos = position.getSide(face!!)
        val power = level.getRedstonePower(pos, face)

        if (power >= 15) {
            return power
        } else {
            val block = level.getBlock(pos)
            return max(
                power.toDouble(),
                (if (block.id == BlockID.REDSTONE_WIRE) block.blockState.specialValue() else 0).toDouble()
            ).toInt()
        }
    }

    protected val powerOnSides: Int
        get() {
            val pos = this.position

            val face = facing
            val face1 = face!!.rotateY()
            val face2 = face.rotateYCCW()
            return max(
                getPowerOnSide(pos.getSide(face1), face1)
                    .toDouble(), getPowerOnSide(pos.getSide(face2), face2).toDouble()
            ).toInt()
        }

    protected fun getPowerOnSide(pos: Vector3, side: BlockFace): Int {
        val block = level.getBlock(pos)
        return if (isAlternateInput(block)) (if (block.id == BlockID.REDSTONE_BLOCK) 15 else (if (block.id == BlockID.REDSTONE_WIRE)
            block.blockState.specialValue()
                .toInt()
        else
            level.getStrongPower(pos, side))) else 0
    }

    override val isPowerSource: Boolean
        get() = true

    protected open fun shouldBePowered(): Boolean {
        return this.calculateInputStrength() > 0
    }

    abstract val facing: BlockFace?

    protected abstract val delay: Int

    protected abstract val unpowered: Block

    protected abstract fun getPowered(): Block

    override var maxY: Double
        get() = position.y + 0.125
        set(maxY) {
            super.maxY = maxY
        }

    override fun canPassThrough(): Boolean {
        return false
    }

    protected open fun isAlternateInput(block: Block): Boolean {
        return block.isPowerSource
    }

    protected open val redstoneSignal: Int
        get() = 15

    override fun getStrongPower(side: BlockFace): Int {
        return getWeakPower(side)
    }

    override fun getWeakPower(face: BlockFace): Int {
        return if (!this.isPowered()) 0 else (if (facing == face) redstoneSignal else 0)
    }

    override fun canBeActivated(): Boolean {
        return true
    }

    open fun isPowered(): Boolean {
        return isPowered
    }

    val isFacingTowardsRepeater: Boolean
        get() {
            val side = facing!!.getOpposite()
            val block = this.getSide(side)
            return block is BlockRedstoneDiode && block.facing != side
        }

    override fun recalculateBoundingBox(): AxisAlignedBB? {
        return SimpleAxisAlignedBB(
            position.x,
            position.y,
            position.z, position.x + 1, position.y + 0.125, position.z + 1
        )
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

    companion object {
        fun isDiode(block: Block?): Boolean {
            return block is BlockRedstoneDiode
        }
    }
}
