package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.CommonPropertyMap
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.math.BlockFace

class BlockLeafLitter(blockState: BlockState = properties.defaultState) : BlockFlowable(blockState) {
    override val properties: BlockProperties
        get() = Companion.properties

    fun isSupportValid(block: Block): Boolean {
        return block.isFullBlock
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
        if (!isSupportValid(block.down())) {
            return false
        }

        if (player != null) {
            setPropertyValue(
                CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION,
                CommonPropertyMap.CARDINAL_BLOCKFACE.inverse()[player.getHorizontalFacing().getOpposite()]!!
            )
        }

        return this.level.setBlock(this.position, this)
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
        val growth = getPropertyValue(CommonBlockProperties.GROWTH)
        if (item.blockId == this.id && growth < 3) {
            setPropertyValue(CommonBlockProperties.GROWTH, growth + 1)
            level.setBlock(this.position, this)
            item.count--
            return true
        }
        return false
    }

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.LEAF_LITTER,
            CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION,
            CommonBlockProperties.GROWTH
        )
    }
}