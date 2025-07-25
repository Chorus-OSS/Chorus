package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.IntPropertyType
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockFace.Companion.fromIndex
import org.chorus_oss.chorus.utils.Faceable

/**
 * Alias piston head
 */
open class BlockPistonArmCollision @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockTransparent(blockstate), Faceable {
    override val name: String
        get() = "Piston Head"

    override val resistance: Double
        get() = 1.5

    override val hardness: Double
        get() = 1.5

    override fun getDrops(item: Item): Array<Item> {
        return Item.EMPTY_ARRAY
    }

    override fun onBreak(item: Item?): Boolean {
        level.setBlock(this.position, get(BlockID.AIR), true, true)
        val side = getSide(blockFace.getOpposite())

        if (side is BlockPistonBase && side.blockFace == this.blockFace) {
            side.onBreak(item)

            val entity = side.blockEntity
            entity?.close()
        }
        return true
    }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (getSide(blockFace.getOpposite()) !is BlockPistonBase) {
                level.setBlock(this.position, BlockAir(), true, false)
            }
            return type
        }
        return 0
    }

    val facing: BlockFace
        get() = blockFace

    override var blockFace: BlockFace
        get() {
            val face =
                fromIndex(getPropertyValue<Int, IntPropertyType>(CommonBlockProperties.FACING_DIRECTION))
            return if (face.horizontalIndex >= 0) face.getOpposite() else face
        }
        set(face) {
            setPropertyValue<Int, IntPropertyType>(CommonBlockProperties.FACING_DIRECTION, face.index)
        }

    override fun canBePushed(): Boolean {
        return false
    }

    override fun canBePulled(): Boolean {
        return false
    }

    override val isSolid: Boolean
        get() = false

    override fun isSolid(side: BlockFace): Boolean {
        return false
    }

    override fun toItem(): Item {
        return Item.AIR
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.PISTON_ARM_COLLISION, CommonBlockProperties.FACING_DIRECTION)
    }
}