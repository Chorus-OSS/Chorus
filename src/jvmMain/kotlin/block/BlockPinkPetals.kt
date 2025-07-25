package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.CommonPropertyMap
import org.chorus_oss.chorus.block.property.type.IntPropertyType
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.particle.BoneMealParticle
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.tags.BlockTags

class BlockPinkPetals @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockFlowable(blockState) {
    override val name: String
        get() = "Pink Petals"

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

        return level.setBlock(this.position, this)
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
        if (item.isFertilizer) {
            if (getPropertyValue(CommonBlockProperties.GROWTH) < 3) {
                setPropertyValue(
                    CommonBlockProperties.GROWTH, getPropertyValue(
                        CommonBlockProperties.GROWTH
                    ) + 1
                )
                level.setBlock(this.position, this)
            } else {
                level.dropItem(this.position, toItem())
            }

            level.addParticle(BoneMealParticle(this.position))
            item.count--
            return true
        }

        if (item.blockId == BlockID.PINK_PETALS && getPropertyValue(CommonBlockProperties.GROWTH) < 3) {
            setPropertyValue(
                CommonBlockProperties.GROWTH, getPropertyValue<Int, IntPropertyType>(
                    CommonBlockProperties.GROWTH
                ) + 1
            )
            level.setBlock(this.position, this)
            item.count--
            return true
        }

        return false
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.PINK_PETALS,
            CommonBlockProperties.GROWTH, CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION
        )


        private fun isSupportValid(block: Block): Boolean {
            return block.`is`(BlockTags.DIRT)
        }
    }
}
