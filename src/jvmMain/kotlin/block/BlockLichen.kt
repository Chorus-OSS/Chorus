package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.IntPropertyType
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.utils.ChorusRandom
import java.util.*

abstract class BlockLichen(blockState: BlockState) : BlockTransparent(blockState) {
    val growthSides: Array<BlockFace>
        get() {
            val returns = BlockFace.entries.filter { side: BlockFace -> this.isGrowthToSide(side) }
            return returns.toTypedArray()
        }

    open fun witherAtSide(side: BlockFace) {
        if (isGrowthToSide(side)) {
            setPropertyValue<Int, IntPropertyType>(
                CommonBlockProperties.MULTI_FACE_DIRECTION_BITS,
                getPropertyValue<Int, IntPropertyType>(CommonBlockProperties.MULTI_FACE_DIRECTION_BITS) xor (1 shl side.indexDUSWNE)
            )
            level.setBlock(this.position, this, true, true)
        }
    }

    fun isGrowthToSide(side: BlockFace): Boolean {
        return ((getPropertyValue<Int, IntPropertyType>(CommonBlockProperties.MULTI_FACE_DIRECTION_BITS) shr side.indexDUSWNE) and 0x1) > 0
    }

    fun growToSide(side: BlockFace) {
        if (!isGrowthToSide(side)) {
            setPropertyValue<Int, IntPropertyType>(
                CommonBlockProperties.MULTI_FACE_DIRECTION_BITS,
                getPropertyValue<Int, IntPropertyType>(CommonBlockProperties.MULTI_FACE_DIRECTION_BITS) or (1 shl side.indexDUSWNE)
            )
            level.setBlock(this.position, this, true, true)
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
        if (!target!!.isSolid && target is BlockLichen) {
            return false
        }

        var currentMeta = 0
        if (block is BlockLichen) {
            currentMeta = block.getPropertyValue<Int, IntPropertyType>(CommonBlockProperties.MULTI_FACE_DIRECTION_BITS)
        }

        setPropertyValue<Int, IntPropertyType>(
            CommonBlockProperties.MULTI_FACE_DIRECTION_BITS,
            currentMeta or (1 shl face.getOpposite().indexDUSWNE)
        )

        if (getPropertyValue<Int, IntPropertyType>(CommonBlockProperties.MULTI_FACE_DIRECTION_BITS) == currentMeta) {
            val sides = BlockFace.entries.toTypedArray()
            val faceStream = Arrays.stream(sides).filter { side: BlockFace ->
                block.getSide(side)
                    .isSolid(side) && !isGrowthToSide(side)
            }
            val optionalFace = faceStream.findFirst()
            if (optionalFace.isPresent) {
                growToSide(optionalFace.get())
                return true
            }

            return false
        }

        level.setBlock(block.position, this, true, true)
        return true
    }

    override fun onUpdate(type: Int): Int {
        for (side in BlockFace.entries) {
            val support = this.getSide(side)
            if (isGrowthToSide(side) && support != null && !support.isSolid) {
                this.witherAtSide(side)
            }
        }
        return super.onUpdate(type)
    }

    override val hardness: Double
        get() = 0.2

    override val resistance: Double
        get() = 1.0

    override fun canPassThrough(): Boolean {
        return true
    }

    override val waterloggingLevel: Int
        get() = 1

    override fun canSilkTouch(): Boolean {
        return true
    }

    override val toolType: Int
        get() = ItemTool.TYPE_AXE

    override val toolTier: Int
        get() = ItemTool.TIER_WOODEN

    override fun canHarvest(item: Item): Boolean {
        return item.isAxe || item.isShears
    }

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override fun sticksToPiston(): Boolean {
        return false
    }

    override fun breaksWhenMoved(): Boolean {
        return true
    }

    override fun canBeReplaced(): Boolean {
        return true
    }

    override fun canBeFlowedInto(): Boolean {
        return true
    }

    override val isSolid: Boolean
        get() = false

    override fun isSolid(side: BlockFace): Boolean {
        return false
    }


    companion object {
        val RANDOM: ChorusRandom = ChorusRandom()
    }
}
