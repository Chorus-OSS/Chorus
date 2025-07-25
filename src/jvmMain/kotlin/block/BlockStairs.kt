package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.CommonPropertyMap
import org.chorus_oss.chorus.block.property.type.BooleanPropertyType
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.SimpleAxisAlignedBB
import org.chorus_oss.chorus.utils.Faceable

abstract class BlockStairs(blockState: BlockState) : BlockTransparent(blockState), Faceable {
    override var minY: Double
        get() = position.y + (if (isUpsideDown) 0.5 else 0.0)
        set(minY) {
            super.minY = minY
        }

    override var maxY: Double
        get() = position.y + (if (isUpsideDown) 1.0 else 0.5)
        set(maxY) {
            super.maxY = maxY
        }

    override fun isSolid(side: BlockFace): Boolean {
        return side == BlockFace.UP && isUpsideDown || side == BlockFace.DOWN && !isUpsideDown
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
            blockFace = player.getDirection()
        }

        if ((fy > 0.5 && face != BlockFace.UP) || face == BlockFace.DOWN) {
            isUpsideDown = true
        }
        level.setBlock(block.position, this, true, true)

        return true
    }

    override fun collidesWithBB(bb: AxisAlignedBB): Boolean {
        val face = blockFace
        var minSlabY = 0.0
        var maxSlabY = 0.5
        var minHalfSlabY = 0.5
        var maxHalfSlabY = 1.0

        if (isUpsideDown) {
            minSlabY = 0.5
            maxSlabY = 1.0
            minHalfSlabY = 0.0
            maxHalfSlabY = 0.5
        }

        if (bb.intersectsWith(
                SimpleAxisAlignedBB(
                    position.x,
                    position.y + minSlabY,
                    position.z,
                    position.x + 1,
                    position.y + maxSlabY,
                    position.z + 1
                )
            )
        ) {
            return true
        }

        return when (face) {
            BlockFace.EAST -> bb.intersectsWith(
                SimpleAxisAlignedBB(
                    position.x + 0.5,
                    position.y + minHalfSlabY,
                    position.z,
                    position.x + 1,
                    position.y + maxHalfSlabY,
                    position.z + 1
                )
            )

            BlockFace.WEST -> bb.intersectsWith(
                SimpleAxisAlignedBB(
                    position.x,
                    position.y + minHalfSlabY,
                    position.z,
                    position.x + 0.5,
                    position.y + maxHalfSlabY,
                    position.z + 1
                )
            )

            BlockFace.SOUTH -> bb.intersectsWith(
                SimpleAxisAlignedBB(
                    position.x,
                    position.y + minHalfSlabY,
                    position.z + 0.5,
                    position.x + 1,
                    position.y + maxHalfSlabY,
                    position.z + 1
                )
            )

            BlockFace.NORTH -> bb.intersectsWith(
                SimpleAxisAlignedBB(
                    position.x,
                    position.y + minHalfSlabY,
                    position.z,
                    position.x + 1,
                    position.y + maxHalfSlabY,
                    position.z + 0.5
                )
            )

            else -> false
        }
    }

    override val waterloggingLevel: Int
        get() = 1

    var isUpsideDown: Boolean
        get() = getPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.UPSIDE_DOWN_BIT)
        set(upsideDown) {
            setPropertyValue<Boolean, BooleanPropertyType>(
                CommonBlockProperties.UPSIDE_DOWN_BIT,
                upsideDown
            )
        }

    override var blockFace: BlockFace
        get() = CommonPropertyMap.EWSN_DIRECTION.inverse()[getPropertyValue(CommonBlockProperties.WEIRDO_DIRECTION)]!!
        set(face) {
            setPropertyValue(
                CommonBlockProperties.WEIRDO_DIRECTION,
                CommonPropertyMap.EWSN_DIRECTION[face]!!
            )
        }
}
