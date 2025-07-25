package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.IntPropertyType
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockFace.Companion.fromIndex
import org.chorus_oss.chorus.utils.Faceable

class BlockLightningRod(blockState: BlockState = properties.defaultState) : BlockTransparent(blockState), Faceable {
    override val name: String
        get() = "LightningRod"

    override val isSolid: Boolean
        get() = false

    override fun isSolid(side: BlockFace): Boolean {
        return false
    }

    override val waterloggingLevel: Int
        get() = 1

    override fun canBeFlowedInto(): Boolean {
        return true
    }

    override val hardness: Double
        get() = 3.0

    override val resistance: Double
        get() = 6.0

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
        this.blockFace = face
        level.setBlock(block.position, this, true, true)
        return true
    }

    override var blockFace: BlockFace
        get() = fromIndex(getPropertyValue<Int, IntPropertyType>(CommonBlockProperties.FACING_DIRECTION))
        set(face) {
            this.setPropertyValue<Int, IntPropertyType>(
                CommonBlockProperties.FACING_DIRECTION,
                face.index
            )
        }

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.LIGHTNING_ROD, CommonBlockProperties.FACING_DIRECTION)
    }
}
