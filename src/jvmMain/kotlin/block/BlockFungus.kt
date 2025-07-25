package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.BlockFlowerPot.FlowerPotBlock
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.particle.BoneMealParticle
import org.chorus_oss.chorus.math.BlockFace

abstract class BlockFungus(blockState: BlockState) : BlockFlowable(blockState), FlowerPotBlock,
    Natural {
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
        if (!isValidSupport(down())) {
            return false
        }
        return super.place(item, block, target, face, fx, fy, fz, player)
    }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL && !isValidSupport(down())) {
            level.useBreakOn(this.position)
            return type
        }

        return 0
    }

    override fun onActivate(
        item: Item,
        player: Player?,
        blockFace: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float
    ): Boolean {
        if (item.isNothing || !item.isFertilizer) {
            return false
        }

        level.addParticle(BoneMealParticle(this.position))

        if (player != null && !player.isCreative) {
            item.count--
        }

        val down = down()
        if (!isValidSupport(down)) {
            level.useBreakOn(this.position)
            return true
        }

        if (!canGrowOn(down)) {
            return true
        }

        grow(player)

        return true
    }

    protected abstract fun canGrowOn(support: Block?): Boolean

    protected fun isValidSupport(support: Block): Boolean {
        return when (support.id) {
            BlockID.GRASS_BLOCK, BlockID.DIRT, BlockID.PODZOL, BlockID.FARMLAND, BlockID.CRIMSON_NYLIUM, BlockID.WARPED_NYLIUM, BlockID.SOUL_SOIL, BlockID.MYCELIUM -> true
            else -> false
        }
    }

    override fun canBeActivated(): Boolean {
        return true
    }

    abstract fun grow(cause: Player?): Boolean

    override val isFertilizable: Boolean
        get() = true
}
