package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.IntPropertyType
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockFace.Companion.fromIndex
import org.chorus_oss.chorus.math.CompassRoseDirection

open class BlockWallSign @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockStandingSign(blockState) {
    override fun getWallSignId() = id

    override fun getStandingSignId() = BlockID.STANDING_SIGN

    override val name: String
        get() = "Wall Sign"

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (getSide(blockFace.getOpposite()).isAir) {
                level.useBreakOn(this.position)
            }
            return Level.BLOCK_UPDATE_NORMAL
        }
        return 0
    }

    override var blockFace: BlockFace
        get() = fromIndex(getPropertyValue<Int, IntPropertyType>(CommonBlockProperties.FACING_DIRECTION))
        set(face) {
            setPropertyValue<Int, IntPropertyType>(CommonBlockProperties.FACING_DIRECTION, face.index)
        }

    override fun getSignDirection(): CompassRoseDirection = blockFace.compassRoseDirection!!

    override fun setSignDirection(direction: CompassRoseDirection): Block {
        blockFace = direction.closestBlockFace
        return this
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.WALL_SIGN, CommonBlockProperties.FACING_DIRECTION)
    }
}
