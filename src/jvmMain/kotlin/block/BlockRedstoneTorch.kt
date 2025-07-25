package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.event.redstone.RedstoneUpdateEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.utils.RedstoneComponent

class BlockRedstoneTorch @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockTorch(blockstate), RedstoneComponent {
    override val name: String
        get() = "Redstone Torch"

    override val lightLevel: Int
        get() = 7

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
        if (!super.place(item, block, target, face, fx, fy, fz, player)) {
            return false
        }

        if (Server.instance.settings.levelSettings.enableRedstone) {
            if (!checkState()) {
                updateAllAroundRedstone(blockFace.getOpposite())
            }

            checkState()
        }

        return true
    }

    override fun getWeakPower(face: BlockFace): Int {
        return if (blockFace != face) 15 else 0
    }

    override fun getStrongPower(side: BlockFace): Int {
        return if (side == BlockFace.DOWN) this.getWeakPower(side) else 0
    }

    override fun onBreak(item: Item?): Boolean {
        if (!super.onBreak(item)) {
            return false
        }

        if (Server.instance.settings.levelSettings.enableRedstone) {
            updateAllAroundRedstone(blockFace.getOpposite())
        }
        return true
    }

    override fun onUpdate(type: Int): Int {
        if (super.onUpdate(type) == 0) {
            if (!Server.instance.settings.levelSettings.enableRedstone) {
                return 0
            }

            if (type == Level.BLOCK_UPDATE_NORMAL || type == Level.BLOCK_UPDATE_REDSTONE) {
                level.scheduleUpdate(this, tickRate())
            } else if (type == Level.BLOCK_UPDATE_SCHEDULED) {
                val ev = RedstoneUpdateEvent(this)
                Server.instance.pluginManager.callEvent(ev)

                if (ev.cancelled) {
                    return 0
                }

                if (checkState()) {
                    return 1
                }
            }
        }

        return 0
    }

    private fun checkState(): Boolean {
        if (isPoweredFromSide) {
            level.setBlock(
                this.position,
                get(BlockID.UNLIT_REDSTONE_TORCH).setPropertyValues(propertyValues),
                false,
                true
            )
            updateAllAroundRedstone(blockFace.getOpposite())
            return true
        }
        return false
    }

    protected val isPoweredFromSide: Boolean
        /**
         * Whether there is a power source in the opposite face of the current face
         */
        get() {
            val face = blockFace.getOpposite()
            val side = this.getSide(face)
            if (side is BlockPistonBase && side.isGettingPower) {
                return true
            }

            return level.isSidePowered(side.position, face)
        }

    override val isPowerSource: Boolean
        get() = true

    override fun tickRate(): Int {
        return 2
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.REDSTONE_TORCH, CommonBlockProperties.TORCH_FACING_DIRECTION)
    }
}
