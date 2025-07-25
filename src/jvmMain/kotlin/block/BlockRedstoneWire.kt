package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.IntPropertyType
import org.chorus_oss.chorus.event.block.BlockRedstoneEvent
import org.chorus_oss.chorus.event.redstone.RedstoneUpdateEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemRedstone
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.utils.RedstoneComponent
import java.util.*
import kotlin.math.max

class BlockRedstoneWire @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockFlowable(blockState), RedstoneComponent {
    override val name: String
        get() = "Redstone Wire"

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
        if (!canBePlacedOn(block.down())) {
            return false
        }

        if (Server.instance.settings.levelSettings.enableRedstone) {
            level.setBlock(block.position, this, true)

            this.updateSurroundingRedstone(true)

            val pos = this.locator

            for (blockFace in BlockFace.Plane.VERTICAL_FACES) {
                RedstoneComponent.updateAroundRedstone(pos.getSide(blockFace), blockFace.getOpposite())
            }

            for (blockFace in BlockFace.Plane.VERTICAL_FACES) {
                this.updateAround(pos.getSide(blockFace), blockFace.getOpposite())
            }

            for (blockFace in BlockFace.Plane.HORIZONTAL_FACES) {
                val p = pos.getSide(blockFace)

                if (level.getBlock(p.position).isNormalBlock) {
                    this.updateAround(p.getSide(BlockFace.UP), BlockFace.DOWN)
                } else {
                    this.updateAround(p.getSide(BlockFace.DOWN), BlockFace.UP)
                }
            }
        } else {
            level.setBlock(block.position, this, true, true)
        }
        return true
    }

    //Update the neighbor's block of the pos location as well as the neighbor's neighbor's block
    private fun updateAround(pos: Locator, face: BlockFace) {
        if (level.getBlock(pos.position).id == BlockID.REDSTONE_WIRE) {
            updateAroundRedstone(face)

            for (side in BlockFace.entries) {
                RedstoneComponent.updateAroundRedstone(pos.getSide(side), side.getOpposite())
            }
        }
    }

    private fun updateSurroundingRedstone(force: Boolean) {
        val pos = this.position

        val meta = this.redStoneSignal
        var maxStrength = meta
        val power = this.indirectPower

        if (power > 0 && power > maxStrength - 1) {
            maxStrength = power
        }

        var strength = 0

        for (face in BlockFace.Plane.HORIZONTAL_FACES) {
            val v = pos.getSide(face)

            if (v.x == this.x && v.z == this.z) {
                continue
            }

            strength = this.getMaxCurrentStrength(v, strength)

            if (this.getMaxCurrentStrength(v.up(), strength) > strength && !level.getBlock(
                    pos.up()
                ).isNormalBlock
            ) {
                strength = this.getMaxCurrentStrength(v.up(), strength)
            }
            if (this.getMaxCurrentStrength(v.down(), strength) > strength && !level.getBlock(
                    v
                ).isNormalBlock
            ) {
                strength = this.getMaxCurrentStrength(v.down(), strength)
            }
        }

        if (strength > maxStrength) {
            maxStrength = strength - 1
        } else if (maxStrength > 0) {
            --maxStrength
        } else {
            maxStrength = 0
        }

        if (power > maxStrength - 1) {
            maxStrength = power
        } else if (power < maxStrength && strength <= maxStrength) {
            maxStrength = max(power.toDouble(), (strength - 1).toDouble()).toInt()
        }

        if (meta != maxStrength) {
            Server.instance.pluginManager.callEvent(BlockRedstoneEvent(this, meta, maxStrength))

            this.redStoneSignal = maxStrength
            level.setBlock(this.position, this, false, true)

            updateAllAroundRedstone()
        } else if (force) {
            for (face in BlockFace.entries) {
                RedstoneComponent.updateAroundRedstone(getSide(face), face.getOpposite())
            }
        }
    }

    private fun getMaxCurrentStrength(pos: Vector3, maxStrength: Int): Int {
        if (level.getBlockIdAt(pos.floorX, pos.floorY, pos.floorZ) != this.id) {
            return maxStrength
        } else {
            val strength = level.getBlockStateAt(pos.floorX, pos.floorY, pos.floorZ).getPropertyValue(
                CommonBlockProperties.REDSTONE_SIGNAL
            )
            return max(strength.toDouble(), maxStrength.toDouble()).toInt()
        }
    }

    override fun onBreak(item: Item?): Boolean {
        val air = get(BlockID.AIR)
        level.setBlock(this.position, air, true, true)

        val pos = this.locator

        if (Server.instance.settings.levelSettings.enableRedstone) {
            this.updateSurroundingRedstone(false)
            level.setBlock(this.position, air, true, true)

            for (blockFace in BlockFace.entries) {
                RedstoneComponent.updateAroundRedstone(pos.getSide(blockFace))
            }

            for (blockFace in BlockFace.Plane.HORIZONTAL_FACES) {
                val p = pos.getSide(blockFace)

                if (level.getBlock(p.position).isNormalBlock) {
                    this.updateAround(p.getSide(BlockFace.UP), BlockFace.DOWN)
                } else {
                    this.updateAround(p.getSide(BlockFace.DOWN), BlockFace.UP)
                }
            }
        }
        return true
    }

    override fun toItem(): Item {
        return ItemRedstone()
    }

    override fun onUpdate(type: Int): Int {
        if (type != Level.BLOCK_UPDATE_NORMAL && type != Level.BLOCK_UPDATE_REDSTONE) {
            return 0
        }

        if (!Server.instance.settings.levelSettings.enableRedstone) {
            return 0
        }

        // Redstone event
        val ev = RedstoneUpdateEvent(this)
        Server.instance.pluginManager.callEvent(ev)
        if (ev.cancelled) {
            return 0
        }

        if (type == Level.BLOCK_UPDATE_NORMAL && !this.canBePlacedOn(down())) {
            level.useBreakOn(this.position)
            return Level.BLOCK_UPDATE_NORMAL
        }

        this.updateSurroundingRedstone(false)

        return Level.BLOCK_UPDATE_NORMAL
    }

    fun canBePlacedOn(support: Block): Boolean {
        return support.isSolid(BlockFace.UP)
    }

    override fun getStrongPower(side: BlockFace): Int {
        return if (this.isPowerSource) getWeakPower(side) else 0
    }

    override fun getWeakPower(face: BlockFace): Int {
        if (!this.isPowerSource) {
            return 0
        } else {
            val power = this.redStoneSignal

            if (power == 0) {
                return 0
            } else if (face == BlockFace.UP) {
                return power
            } else {
                val faces: EnumSet<BlockFace> = EnumSet.noneOf(BlockFace::class.java)

                for (face in BlockFace.Plane.HORIZONTAL_FACES) {
                    if (this.isPowerSourceAt(face)) {
                        faces.add(face)
                    }
                }

                return if (face.axis.isHorizontal && faces.isEmpty()) {
                    power
                } else if (faces.contains(face) && !faces.contains(face.rotateYCCW()) && !faces.contains(face.rotateY())) {
                    power
                } else {
                    0
                }
            }
        }
    }

    private fun isPowerSourceAt(side: BlockFace): Boolean {
        val pos = this.position
        val v = pos.getSide(side)
        val block = level.getBlock(v)
        val flag = block.isNormalBlock
        val flag1 = level.getBlock(pos.up()).isNormalBlock
        return !flag1 && flag && canConnectUpwardsTo(this.level, v.up()) || (canConnectTo(
            block, side
        ) || !flag && canConnectUpwardsTo(this.level, block.down().position))
    }

    override val isPowerSource: Boolean
        /** */
        get() = getPropertyValue<Int, IntPropertyType>(CommonBlockProperties.REDSTONE_SIGNAL) > 0

    var redStoneSignal: Int
        get() = getPropertyValue<Int, IntPropertyType>(CommonBlockProperties.REDSTONE_SIGNAL)
        set(signal) {
            setPropertyValue<Int, IntPropertyType>(CommonBlockProperties.REDSTONE_SIGNAL, signal)
        }

    private val indirectPower: Int
        get() {
            var power = 0
            val pos = this.position

            for (face in BlockFace.entries) {
                val blockPower = this.getIndirectPower(pos.getSide(face), face)

                if (blockPower >= 15) {
                    return 15
                }

                if (blockPower > power) {
                    power = blockPower
                }
            }

            return power
        }

    private fun getIndirectPower(pos: Vector3, face: BlockFace): Int {
        val block = level.getBlock(pos)
        if (block.id == BlockID.REDSTONE_WIRE) {
            return 0
        }
        return if (block.isNormalBlock) getStrongPower(pos) else block.getWeakPower(face)
    }

    private fun getStrongPower(pos: Vector3): Int {
        var i = 0
        for (face in BlockFace.entries) {
            i = max(i.toDouble(), getStrongPower(pos.getSide(face), face).toDouble()).toInt()

            if (i >= 15) {
                return i
            }
        }
        return i
    }

    private fun getStrongPower(pos: Vector3, direction: BlockFace): Int {
        val block = level.getBlock(pos)

        if (block.id == BlockID.REDSTONE_WIRE) {
            return 0
        }

        return block.getStrongPower(direction)
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.REDSTONE_WIRE, CommonBlockProperties.REDSTONE_SIGNAL)

        protected fun canConnectUpwardsTo(level: Level, pos: Vector3): Boolean {
            return canConnectTo(level.getBlock(pos), null)
        }

        protected fun canConnectTo(block: Block, side: BlockFace?): Boolean {
            if (block.id == BlockID.REDSTONE_WIRE) {
                return true
            } else if (BlockRedstoneDiode.Companion.isDiode(block)) {
                val face = (block as BlockRedstoneDiode).facing
                return face == side || face!!.getOpposite() == side
            } else {
                return block.isPowerSource && side != null
            }
        }
    }
}
