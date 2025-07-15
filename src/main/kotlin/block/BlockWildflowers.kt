package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.CommonPropertyMap
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.particle.BoneMealParticle
import org.chorus_oss.chorus.math.BlockFace

class BlockWildflowers(blockState: BlockState = properties.defaultState) : BlockFlower(blockState) {
    override val properties: BlockProperties
        get() = Companion.properties

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
        if (!BlockSweetBerryBush.isSupportValid(block.down())) return false

        if (player != null) {
            setPropertyValue(
                CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION,
                CommonPropertyMap.CARDINAL_BLOCKFACE.inverse().get(player.getHorizontalFacing().getOpposite())!!
            )
        }

        return this.level.setBlock(this.position, this)
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

        if (item.blockId == this.id && getPropertyValue(CommonBlockProperties.GROWTH) < 3) {
            setPropertyValue(
                CommonBlockProperties.GROWTH, getPropertyValue(
                    CommonBlockProperties.GROWTH
                ) + 1
            )
            level.setBlock(this.position, this)
            item.count--
            return true
        }

        return false
    }

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.WILDFLOWERS,
            CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION,
            CommonBlockProperties.GROWTH
        )
    }
}