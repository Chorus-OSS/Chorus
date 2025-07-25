package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server.Companion.instance
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.event.block.BlockGrowEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.particle.BoneMealParticle
import org.chorus_oss.chorus.math.BlockFace
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.min


abstract class BlockCrops(blockState: BlockState) : BlockFlowable(blockState) {
    val maxGrowth: Int
        get() = CommonBlockProperties.GROWTH.max

    var growth: Int
        get() = getPropertyValue(CommonBlockProperties.GROWTH)
        set(growth) {
            setPropertyValue(CommonBlockProperties.GROWTH, growth)
        }

    val isFullyGrown: Boolean
        get() = growth >= maxGrowth

    override fun canBeActivated(): Boolean {
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
        if (block.down().id == BlockID.FARMLAND) {
            level.setBlock(block.position, this, direct = true, update = true)
            return true
        }
        return false
    }

    override fun onActivate(
        item: Item,
        player: Player?,
        blockFace: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float
    ): Boolean {
        //Bone meal
        if (item.isFertilizer) {
            val max = maxGrowth
            var growth = growth
            if (growth < max) {
                val block = clone() as BlockCrops
                growth += ThreadLocalRandom.current().nextInt(3) + 2
                block.growth = min(growth.toDouble(), max.toDouble()).toInt()
                val ev = BlockGrowEvent(this, block)
                instance.pluginManager.callEvent(ev)

                if (ev.cancelled) {
                    return false
                }

                level.setBlock(this.position, ev.newState, direct = false, update = true)
                level.addParticle(BoneMealParticle(this.position))

                if (player != null && !player.isCreative) {
                    item.count--
                }
            }

            return true
        }

        return false
    }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (down().id != BlockID.FARMLAND) {
                level.useBreakOn(this.position)
                return Level.BLOCK_UPDATE_NORMAL
            }
        } else if (type == Level.BLOCK_UPDATE_RANDOM) {
            if (ThreadLocalRandom.current()
                    .nextInt(2) == 1 && level.getFullLight(this.position) >= MIN_LIGHT_LEVEL
            ) {
                val growth = growth
                if (growth < maxGrowth) {
                    val block = clone() as BlockCrops
                    block.growth = growth + 1
                    val ev = BlockGrowEvent(this, block)
                    instance.pluginManager.callEvent(ev)

                    if (!ev.cancelled) {
                        level.setBlock(this.position, ev.newState, direct = false, update = true)
                    } else {
                        return Level.BLOCK_UPDATE_RANDOM
                    }
                }
            } else {
                return Level.BLOCK_UPDATE_RANDOM
            }
        }

        return 0
    }

    override val isFertilizable: Boolean
        get() = true

    companion object {
        const val MIN_LIGHT_LEVEL: Int = 9
    }
}
