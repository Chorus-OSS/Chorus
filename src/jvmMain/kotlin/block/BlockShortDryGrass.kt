package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.particle.BoneMealParticle
import org.chorus_oss.chorus.math.BlockFace

class BlockShortDryGrass(blockState: BlockState = properties.defaultState) : BlockFlowable(blockState) {
    override val properties: BlockProperties
        get() = Companion.properties

    override fun canBeReplaced(): Boolean {
        return true
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
        if (BlockSweetBerryBush.isSupportValid(block.down())) {
            this.level.setBlock(block.position, this, true)
            return true
        }
        return false
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
            if (player != null && !player.isCreative) {
                item.count--
            }

            val tallDryGrass = BlockTallDryGrass()

            this.level.addParticle(BoneMealParticle(this.position))
            this.level.setBlock(this.position, tallDryGrass, direct = true, update = false)
            return true
        }
        return false
    }

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.SHORT_DRY_GRASS)
    }
}