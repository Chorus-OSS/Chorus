package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.utils.Rail
import org.chorus_oss.chorus.utils.Rail.Orientation.Companion.byMetadata
import org.chorus_oss.chorus.utils.Rail.isRailBlock
import org.chorus_oss.chorus.utils.RedstoneComponent
import org.chorus_oss.chorus.utils.RedstoneComponent.Companion.updateAroundRedstone

class BlockGoldenRail @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockRail(blockstate), RedstoneComponent {
    init {
        canBePowered = true
    }

    override val name: String
        get() = "Powered Rail"

    override fun onUpdate(type: Int): Int {
        // Warning: I didn't recommend this on slow networks server or slow client
        //          Network below 86Kb/s. This will became unresponsive to clients
        //          When updating the block state. Espicially on the world with many rails.
        //          Trust me, I tested this on my server.
        if (type == Level.BLOCK_UPDATE_NORMAL || type == Level.BLOCK_UPDATE_REDSTONE || type == Level.BLOCK_UPDATE_SCHEDULED) {
            if (super.onUpdate(type) == Level.BLOCK_UPDATE_NORMAL) {
                return 0 // Already broken
            }

            if (!Server.instance.settings.levelSettings.enableRedstone) {
                return 0
            }
            val wasPowered = isActive()
            val isPowered = this.isGettingPower
                    || checkSurrounding(this.position, true, 0)
                    || checkSurrounding(this.position, false, 0)

            // Avoid Block mistake
            if (wasPowered != isPowered) {
                setIsActive(isPowered)
                updateAroundRedstone(down(), BlockFace.UP, BlockFace.DOWN)
                if (getOrientation()!!.isAscending) {
                    updateAroundRedstone(up(), BlockFace.UP, BlockFace.DOWN)
                }
            }
            return type
        }
        return 0
    }

    override fun afterRemoval(newBlock: Block, update: Boolean) {
        updateAroundRedstone(down())
        if (getOrientation()!!.isAscending) {
            updateAroundRedstone(up())
        }
        super.afterRemoval(newBlock, update)
    }

    /**
     * Check the surrounding of the rail
     *
     * @param pos      The rail position
     * @param relative The relative of the rail that will be checked
     * @param power    The count of the rail that had been counted
     * @return Boolean of the surrounding area. Where the powered rail on!
     */
    private fun checkSurrounding(pos: Vector3, relative: Boolean, power: Int): Boolean {
        // The powered rail can power up to 8 blocks only
        if (power >= 8) {
            return false
        }
        // The position of the floor numbers
        var dx = pos.floorX
        var dy = pos.floorY
        var dz = pos.floorZ
        // First: get the base block
        val block: BlockRail
        val block2 = level.getBlock(Vector3(dx.toDouble(), dy.toDouble(), dz.toDouble()))

        // Second: check if the rail is Powered rail
        if (isRailBlock(block2)) {
            block = block2 as BlockRail
        } else {
            return false
        }

        // Used to check if the next ascending rail should be what
        val base = block.getOrientation()
        var onStraight = true
        // Third: Recalculate the base position
        when (base) {
            Rail.Orientation.STRAIGHT_NORTH_SOUTH -> {
                if (relative) {
                    dz++
                } else {
                    dz--
                }
            }

            Rail.Orientation.STRAIGHT_EAST_WEST -> {
                if (relative) {
                    dx--
                } else {
                    dx++
                }
            }

            Rail.Orientation.ASCENDING_EAST -> {
                if (relative) {
                    dx--
                } else {
                    dx++
                    dy++
                    onStraight = false
                }
            }

            Rail.Orientation.ASCENDING_WEST -> {
                if (relative) {
                    dx--
                    dy++
                    onStraight = false
                } else {
                    dx++
                }
            }

            Rail.Orientation.ASCENDING_NORTH -> {
                if (relative) {
                    dz++
                } else {
                    dz--
                    dy++
                    onStraight = false
                }
            }

            Rail.Orientation.ASCENDING_SOUTH -> {
                if (relative) {
                    dz++
                    dy++
                    onStraight = false
                } else {
                    dz--
                }
            }

            else -> {
                // Unable to determinate the rail orientation
                // Wrong rail?
                return false
            }
        }
        // Next check if the rail is on power state
        return canPowered(Vector3(dx.toDouble(), dy.toDouble(), dz.toDouble()), base, power, relative)
                || onStraight && canPowered(Vector3(dx.toDouble(), dy - 1.0, dz.toDouble()), base, power, relative)
    }

    protected fun canPowered(pos: Vector3, state: Rail.Orientation, power: Int, relative: Boolean): Boolean {
        val block = level.getBlock(pos) as? BlockGoldenRail ?: return false
        // What! My block is air??!! Impossible! XD

        // Sometimes the rails are different orientation
        val base = block.getOrientation()

        // Possible way how to know when the rail is activated is rail were directly powered
        // OR recheck the surrounding... Which will returns here =w=
        return (state != Rail.Orientation.STRAIGHT_EAST_WEST
                || base != Rail.Orientation.STRAIGHT_NORTH_SOUTH && base != Rail.Orientation.ASCENDING_NORTH && base != Rail.Orientation.ASCENDING_SOUTH)
                && (state != Rail.Orientation.STRAIGHT_NORTH_SOUTH
                || base != Rail.Orientation.STRAIGHT_EAST_WEST && base != Rail.Orientation.ASCENDING_EAST && base != Rail.Orientation.ASCENDING_WEST)
                && (block.isGettingPower || checkSurrounding(pos, relative, power + 1))
    }

    override fun getOrientation(): Rail.Orientation? {
        return byMetadata(getPropertyValue(CommonBlockProperties.RAIL_DIRECTION_6))
    }

    override fun setIsActive(active: Boolean) {
        setRailActive(active)
        level.setBlock(this.position, this, direct = true, update = true)
    }

    override fun isActive(): Boolean {
        return getPropertyValue(CommonBlockProperties.RAIL_DATA_BIT)
    }

    override fun setRailActive(active: Boolean) {
        setPropertyValue(CommonBlockProperties.RAIL_DATA_BIT, active)
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(
                BlockID.GOLDEN_RAIL,
                CommonBlockProperties.RAIL_DATA_BIT,
                CommonBlockProperties.RAIL_DIRECTION_6
            )
    }
}