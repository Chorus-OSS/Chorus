package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.IntPropertyType
import org.chorus_oss.chorus.blockentity.BlockEntityDecoratedPot
import org.chorus_oss.chorus.blockentity.BlockEntityID
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockFace.Companion.fromHorizontalIndex
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.utils.Faceable
import java.util.*

class BlockDecoratedPot(blockState: BlockState = properties.defaultState) : BlockFlowable(blockState), Faceable,
    BlockEntityHolder<BlockEntityDecoratedPot> {
    override val name: String
        get() = "Decorated Pot"

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
        val nbt = CompoundTag()

        nbt.putString("id", BlockEntityID.DECORATED_POT)
        nbt.putByte("isMovable", 1)

        if (item!!.namedTag != null) {
            val customData = item.namedTag!!.tags
            for ((key, value) in customData) {
                nbt.put(key, value)
            }
        }

        nbt.putInt("x", position.x.toInt())
        nbt.putInt("y", position.y.toInt())
        nbt.putInt("z", position.y.toInt())

        this.blockFace = player!!.getDirection().getOpposite()
        return BlockEntityHolder.setBlockAndCreateEntity(this, direct = true, update = true, initialData = nbt) != null
    }

    override fun getBlockEntityClass(): Class<out BlockEntityDecoratedPot> {
        return BlockEntityDecoratedPot::class.java
    }

    override fun getBlockEntityType(): String {
        return BlockEntityID.DECORATED_POT
    }

    override var blockFace: BlockFace
        get() = fromHorizontalIndex(getPropertyValue<Int, IntPropertyType>(CommonBlockProperties.DIRECTION))
        set(face) {
            setPropertyValue<Int, IntPropertyType>(
                CommonBlockProperties.DIRECTION,
                Objects.requireNonNullElse(face, BlockFace.SOUTH).horizontalIndex
            )
        }

    override fun canPassThrough(): Boolean {
        return false
    }

    override fun canBeFlowedInto(): Boolean {
        return false
    }

    override fun recalculateBoundingBox(): AxisAlignedBB {
        return this
    }

    override val waterloggingLevel: Int
        get() = 1

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.DECORATED_POT, CommonBlockProperties.DIRECTION)
    }
}