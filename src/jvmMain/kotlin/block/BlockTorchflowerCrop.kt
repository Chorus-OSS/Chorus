package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server.Companion.instance
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.event.block.BlockGrowEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.particle.BoneMealParticle
import org.chorus_oss.chorus.math.BlockFace
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.min

class BlockTorchflowerCrop @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockCrops(blockstate) {
    override val name: String
        get() = "Torchflower Crop"

    override fun toItem(): Item {
        return Item.get(ItemID.TORCHFLOWER_SEEDS)
    }

    override val itemId: String
        get() = ItemID.TORCHFLOWER_SEEDS

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

            if (growth == 1) {
                val block = BlockTorchflower()
                val ev = BlockGrowEvent(this, block)
                instance.pluginManager.callEvent(ev)


                level.setBlock(this.position, ev.newState, false, true)
                level.addParticle(BoneMealParticle(this.position))

                if (player != null && !player.isCreative) {
                    item.count--
                }
                return true
            }
            if (growth < max) {
                val block = clone() as BlockTorchflowerCrop
                growth += 1
                block.growth = min(growth.toDouble(), max.toDouble()).toInt()
                val ev = BlockGrowEvent(this, block)
                instance.pluginManager.callEvent(ev)

                if (ev.cancelled) {
                    return false
                }

                level.setBlock(this.position, ev.newState, false, true)
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
                if (growth == 1) {
                    val block = BlockTorchflower()
                    val ev = BlockGrowEvent(this, block)
                    instance.pluginManager.callEvent(ev)

                    if (ev.cancelled) {
                        return 0
                    } else {
                        level.setBlock(this.position, ev.newState, false, true)
                        return Level.BLOCK_UPDATE_RANDOM
                    }
                }
                if (growth < maxGrowth) {
                    val block = clone() as BlockTorchflowerCrop
                    block.growth = growth + 1
                    val ev = BlockGrowEvent(this, block)
                    instance.pluginManager.callEvent(ev)

                    if (!ev.cancelled) {
                        level.setBlock(this.position, ev.newState, false, true)
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

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.TORCHFLOWER_CROP, CommonBlockProperties.GROWTH)
    }
}