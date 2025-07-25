package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.BooleanPropertyType
import org.chorus_oss.chorus.block.property.type.IntPropertyType
import org.chorus_oss.chorus.blockentity.BlockEntityHangingSign
import org.chorus_oss.chorus.blockentity.BlockEntityID
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockFace.Companion.fromIndex
import org.chorus_oss.chorus.math.CompassRoseDirection
import org.chorus_oss.chorus.math.CompassRoseDirection.Companion.from
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.utils.Loggable

import kotlin.math.floor


abstract class BlockHangingSign(blockState: BlockState) : BlockSignBase(blockState),
    BlockEntityHolder<BlockEntityHangingSign>, Loggable {
    override fun getBlockEntityClass(): Class<out BlockEntityHangingSign> = BlockEntityHangingSign::class.java

    override fun getBlockEntityType(): String = BlockEntityID.HANGING_SIGN

    override val boundingBox: AxisAlignedBB?
        get() = null //01 23 45

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (isHanging) {
                if (up().isAir) {
                    level.useBreakOn(this.position)
                    return Level.BLOCK_UPDATE_NORMAL
                }
            } else {
                if (checkGroundBlock() == null) {
                    level.useBreakOn(this.position)
                    return Level.BLOCK_UPDATE_NORMAL
                }
            }
        }
        return 0
    }

    val isHanging: Boolean
        get() = getPropertyValue(CommonBlockProperties.HANGING)

    val isAttached: Boolean
        get() = getPropertyValue(CommonBlockProperties.ATTACHED_BIT)

    override fun getSignDirection(): CompassRoseDirection {
        return if (isHanging && isAttached) {
            from(getPropertyValue(CommonBlockProperties.GROUND_SIGN_DIRECTION))
        } else {
            fromIndex(getPropertyValue(CommonBlockProperties.FACING_DIRECTION)).compassRoseDirection!!
        }
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
        var face1 = face
        if (player != null && !player.isSneaking() && target is BlockSignBase) {
            return false
        }
        if (face1 == BlockFace.UP) {
            val blockFace = checkGroundBlock() ?: return false
            face1 = blockFace
        }
        if (target is BlockHangingSign && face1 != BlockFace.DOWN) {
            return false
        }

        val layer0 = level.getBlock(this.position, 0)
        val layer1 = level.getBlock(this.position, 1)

        val nbt = CompoundTag()

        if (face1 == BlockFace.DOWN) {
            this.setPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.HANGING, true)
            val direction = from(
                floor((((player?.rotation?.yaw ?: 0.0) + 180) * 16 / 360) + 0.5).toInt() and 0x0f
            )
            if ((player != null && player.isSneaking()) || target is BlockThin || target is BlockChain || target is BlockHangingSign) {
                this.setPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.ATTACHED_BIT, true)
                this.setPropertyValue<Int, IntPropertyType>(
                    CommonBlockProperties.GROUND_SIGN_DIRECTION,
                    direction.index
                )
                level.setBlock(block.position, this, true)
            } else {
                this.setPropertyValue<Int, IntPropertyType>(
                    CommonBlockProperties.FACING_DIRECTION,
                    direction.closestBlockFace.index
                )
                level.setBlock(block.position, this, true)
            }
        } else {
            this.setPropertyValue<Int, IntPropertyType>(CommonBlockProperties.FACING_DIRECTION, face1.rotateY().index)
            level.setBlock(block.position, this, true)
        }
        if (item!!.hasCustomBlockData()) {
            for ((key, value) in item.customBlockData!!.entrySet) {
                nbt.put(key, value)
            }
        }

        try {
            createBlockEntity(nbt)
            player?.openSignEditor(this.position, true)
            return true
        } catch (e: Exception) {
            log.warn("Failed to create block entity {} at {}", getBlockEntityType(), locator, e)
            level.setBlock(layer0.position, 0, layer0, true)
            level.setBlock(layer1.position, 0, layer1, true)
            return false
        }
    }

    private fun checkGroundBlock(): BlockFace? {
        if (getSide(BlockFace.NORTH, 1).isSolid) return BlockFace.NORTH
        if (getSide(BlockFace.SOUTH, 1).isSolid) return BlockFace.SOUTH
        if (getSide(BlockFace.WEST, 1).isSolid) return BlockFace.WEST
        if (getSide(BlockFace.EAST, 1).isSolid) return BlockFace.EAST
        return null
    }
}
