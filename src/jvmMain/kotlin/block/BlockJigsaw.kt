package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.IntPropertyType
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockFace.Companion.fromIndex
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.utils.Faceable
import kotlin.math.abs

class BlockJigsaw @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockSolid(blockstate), Faceable {
    override val name: String
        get() = "Jigsaw"

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override val resistance: Double
        get() = 18000000.0

    override val hardness: Double
        get() = -1.0

    override fun isBreakable(vector: Vector3, layer: Int, face: BlockFace?, item: Item?, player: Player?): Boolean {
        return false
    }

    override fun canBePushed(): Boolean {
        return false
    }

    override var blockFace: BlockFace
        get() = fromIndex(getPropertyValue<Int, IntPropertyType>(CommonBlockProperties.FACING_DIRECTION))
        set(face) {
            setPropertyValue<Int, IntPropertyType>(CommonBlockProperties.FACING_DIRECTION, face.index)
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
        if (abs(player!!.position.x - position.x) < 2 && abs(player.position.z - position.z) < 2) {
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
        level.setBlock(block.position, this, true, false)

        return super.place(item, block, target, face, fx, fy, fz, player)
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.JIGSAW,
            CommonBlockProperties.FACING_DIRECTION,
            CommonBlockProperties.ROTATION
        )
    }
}
