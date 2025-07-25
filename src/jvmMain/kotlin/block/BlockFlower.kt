package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.BlockFlowerPot.FlowerPotBlock
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.particle.BoneMealParticle
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.tags.BlockTags
import java.util.concurrent.ThreadLocalRandom

/**
 * The default is red flower, but there are other flower variants
 */
abstract class BlockFlower(blockstate: BlockState) : BlockFlowable(blockstate), FlowerPotBlock,
    Natural {
    open fun canPlantOn(block: Block): Boolean {
        return isSupportValid(block)
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
        val down = this.down()
        if (canPlantOn(down)) {
            level.setBlock(block.position, this, true)

            return true
        }
        return false
    }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (!canPlantOn(down())) {
                level.useBreakOn(this.position)

                return Level.BLOCK_UPDATE_NORMAL
            }
        }

        return 0
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
        if (item.isFertilizer) { //Bone meal
            if (player != null && (player.gamemode and 0x01) == 0) {
                item.count--
            }

            level.addParticle(BoneMealParticle(this.position))

            for (i in 0..7) {
                val vec = position.add(
                    ThreadLocalRandom.current().nextInt(-3, 4).toDouble(),
                    ThreadLocalRandom.current().nextInt(-1, 2).toDouble(),
                    ThreadLocalRandom.current().nextInt(-3, 4).toDouble()
                )

                if (level.getBlock(vec).id == BlockID.AIR && level.getBlock(vec.down()).id == BlockID.GRASS_BLOCK && vec.y >= level.dimensionData.minHeight && vec.y < level.dimensionData.maxHeight) {
                    if (ThreadLocalRandom.current().nextInt(10) == 0) {
                        level.setBlock(vec, this.uncommonFlower, true)
                    } else {
                        level.setBlock(vec, this, true)
                    }
                }
            }

            return true
        }

        return false
    }

    open val uncommonFlower: Block
        get() = get(BlockID.DANDELION)

    companion object {
        @JvmStatic
        fun isSupportValid(block: Block): Boolean {
            return block.`is`(BlockTags.DIRT)
        }
    }
}
