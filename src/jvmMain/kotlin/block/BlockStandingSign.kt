package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.blockentity.BlockEntityID
import org.chorus_oss.chorus.blockentity.BlockEntitySign
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemOakSign
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.CompassRoseDirection
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.utils.Loggable

import kotlin.math.floor

/**
 * Alias post sign
 */

open class BlockStandingSign @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockSignBase(blockstate), BlockEntityHolder<BlockEntitySign>, Loggable {

    open fun getStandingSignId(): String? = id

    open fun getWallSignId(): String = BlockID.WALL_SIGN

    override fun getBlockEntityClass(): Class<out BlockEntitySign> = BlockEntitySign::class.java

    override fun getBlockEntityType(): String = BlockEntityID.SIGN

    override val boundingBox: AxisAlignedBB?
        get() = null

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (down().isAir) {
                level.useBreakOn(this.position)
                return Level.BLOCK_UPDATE_NORMAL
            }
        }
        return 0
    }

    override fun toItem(): Item {
        return ItemOakSign()
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
        if (face == BlockFace.DOWN) {
            return false
        }
        if (player != null && !player.isSneaking() && target is BlockSignBase) {
            return false
        }

        val layer0 = level.getBlock(this.position, 0)
        val layer1 = level.getBlock(this.position, 1)

        val nbt = CompoundTag()

        if (face == BlockFace.UP) {
            val direction: CompassRoseDirection = CompassRoseDirection.from(
                floor(
                    (((player?.rotation?.yaw
                        ?: 0.0) + 180) * 16 / 360) + 0.5
                ).toInt() and 0x0f
            )
            val post = get(getStandingSignId()!!)
            post.setPropertyValue(CommonBlockProperties.GROUND_SIGN_DIRECTION, direction.index)
            level.setBlock(block.position, post, true)
        } else {
            val wall = get(getWallSignId())
            wall.setPropertyValue(CommonBlockProperties.FACING_DIRECTION, face.index)
            level.setBlock(block.position, wall, true)
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

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.STANDING_SIGN, CommonBlockProperties.GROUND_SIGN_DIRECTION)
    }
}