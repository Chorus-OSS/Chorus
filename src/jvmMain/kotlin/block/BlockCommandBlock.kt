package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server.Companion.instance
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.blockentity.BlockEntityCommandBlock
import org.chorus_oss.chorus.blockentity.BlockEntityID
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockFace.Companion.fromIndex
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.utils.Faceable
import kotlin.math.abs
import kotlin.math.min

//special thanks to wode
open class BlockCommandBlock @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockSolid(blockstate), Faceable, BlockEntityHolder<BlockEntityCommandBlock> {
    override val resistance: Double
        get() = 6000000.0

    override fun canBePushed(): Boolean {
        return false
    }

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override fun isBreakable(vector: Vector3, layer: Int, face: BlockFace?, item: Item?, player: Player?): Boolean {
        return player != null && player.isCreative
    }

    override var blockFace: BlockFace
        get() = fromIndex(getPropertyValue(CommonBlockProperties.FACING_DIRECTION))
        set(face) {
            setPropertyValue(CommonBlockProperties.FACING_DIRECTION, face.index)
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
            if (!player.isCreative) return false
            if (abs(player.position.floorX - position.x) < 2 && abs(player.position.floorZ - position.z) < 2) {
                val y = player.position.y + player.getEyeHeight()
                if (y - position.y > 2) {
                    this.blockFace = BlockFace.UP
                } else if (position.y - y > 0) {
                    this.blockFace = BlockFace.DOWN
                } else {
                    this.blockFace = player.getHorizontalFacing().getOpposite()
                }
            } else {
                this.blockFace = player.getHorizontalFacing().getOpposite()
            }
        } else {
            this.blockFace = BlockFace.DOWN
        }
        return BlockEntityHolder.setBlockAndCreateEntity(
            this,
            direct = true,
            update = true
        ) != null
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
        if (player != null) {
            val itemInHand = player.inventory.itemInHand
            if (player.isSneaking() && !(itemInHand.isTool || itemInHand.isNothing) || !instance.settings.gameplaySettings.enableCommandBlocks
            ) {
                return false
            }
            val tile: BlockEntityCommandBlock = this.getOrCreateBlockEntity()
            tile.spawnTo(player)
            player.addWindow(tile.inventory)
        }
        return true
    }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL || type == Level.BLOCK_UPDATE_REDSTONE) {
            val tile: BlockEntityCommandBlock = this.blockEntity ?: return super.onUpdate(type)
            if (this.isGettingPower) {
                if (!tile.isPowered) {
                    tile.setPowered()
                    tile.trigger()
                }
            } else {
                tile.isPowered = false
            }
        }
        return super.onUpdate(type)
    }

    override fun hasComparatorInputOverride(): Boolean {
        return true
    }

    override val comparatorInputOverride: Int
        get() = min(
            getOrCreateBlockEntity().successCount,
            0xf
        )

    override fun getBlockEntityClass(): Class<out BlockEntityCommandBlock> {
        return BlockEntityCommandBlock::class.java
    }

    override fun getBlockEntityType(): String {
        return BlockEntityID.COMMAND_BLOCK
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.COMMAND_BLOCK,
            CommonBlockProperties.CONDITIONAL_BIT,
            CommonBlockProperties.FACING_DIRECTION
        )
    }
}
