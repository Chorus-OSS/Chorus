package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Server.Companion.instance
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.event.block.BlockGrowEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.Item.Companion.get
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockFace.Companion.fromIndex
import org.chorus_oss.chorus.utils.Faceable
import java.util.concurrent.ThreadLocalRandom

abstract class BlockCropsStem(blockstate: BlockState) : BlockCrops(blockstate), Faceable {
    abstract val strippedState: BlockState?

    abstract val fruitId: String

    abstract val seedsId: String?

    override var blockFace: BlockFace
        get() = facing
        set(face) {
            setPropertyValue(CommonBlockProperties.FACING_DIRECTION, face.index)
        }

    val facing: BlockFace
        get() = fromIndex(getPropertyValue(CommonBlockProperties.FACING_DIRECTION))

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (down().id !== BlockID.FARMLAND) {
                level.useBreakOn(this.position)
                return Level.BLOCK_UPDATE_NORMAL
            }
            var blockFace = blockFace
            if (blockFace.axis.isHorizontal && getSide(blockFace).id !== fruitId) {
                blockFace = BlockFace.DOWN
                level.setBlock(this.position, this)
                return Level.BLOCK_UPDATE_NORMAL
            }
            return 0
        }

        if (type != Level.BLOCK_UPDATE_RANDOM) {
            return 0
        }

        if (ThreadLocalRandom.current().nextInt(1, 3) != 1
            || level.getFullLight(this.position) < MIN_LIGHT_LEVEL
        ) {
            return Level.BLOCK_UPDATE_RANDOM
        }

        val growth = growth
        if (growth < CommonBlockProperties.GROWTH.max) {
            val block = this.clone()
            block.growth = growth + 1
            val ev = BlockGrowEvent(this, block)
            instance.pluginManager.callEvent(ev)
            if (!ev.cancelled) {
                level.setBlock(this.position, ev.newState, true)
            }
            return Level.BLOCK_UPDATE_RANDOM
        }

        growFruit()
        return Level.BLOCK_UPDATE_RANDOM
    }

    fun growFruit(): Boolean {
        val fruitId = fruitId
        for (face in BlockFace.Plane.HORIZONTAL_FACES) {
            val b = this.getSide(face)
            if (b.id == fruitId) {
                return false
            }
        }

        val sideFace = BlockFace.Plane.HORIZONTAL.random()
        val side = this.getSide(sideFace)
        val d = side.down()
        if (side.isAir && (d.id == BlockID.FARMLAND || d.id == BlockID.GRASS_BLOCK || d.id == BlockID.DIRT)) {
            val ev = BlockGrowEvent(side, get(fruitId))
            instance.pluginManager.callEvent(ev)
            if (!ev.cancelled) {
                level.setBlock(side.position, ev.newState, true)
                blockFace = sideFace
                level.setBlock(this.position, this, true)
            }
        }
        return true
    }

    override fun toItem(): Item {
        return Item.get(seedsId!!)
    }

    override fun getDrops(item: Item): Array<Item> {
        val dropChance = dropChances[growth.coerceIn(0, dropChances.size)]

        val dice = ThreadLocalRandom.current().nextDouble()
        var count = 0
        while (dice > dropChance[count]) {
            count++
        }

        if (count == 0) {
            return Item.EMPTY_ARRAY
        }

        return arrayOf(
            get(seedsId!!, 0, count)
        )
    }

    override fun clone(): BlockCropsStem {
        return super.clone() as BlockCropsStem
    }


    companion object {
        //https://minecraft.wiki/w/Melon_Seeds#Breaking
        private val dropChances = arrayOf(
            doubleArrayOf(.8130, .1742, .0124, .0003),  //0
            doubleArrayOf(.6510, .3004, .0462, .0024),  //1
            doubleArrayOf(.5120, .3840, .0960, .0080),  //2
            doubleArrayOf(.3944, .4302, .1564, .0190),  //3
            doubleArrayOf(.2913, .4444, .2222, .0370),  //4
            doubleArrayOf(.2160, .4320, .2880, .0640),  //5
            doubleArrayOf(.1517, .3982, .3484, .1016),  //6
            doubleArrayOf(.1016, .3484, .3982, .1517) //7
        )

        init {
            for (dropChance in dropChances) {
                var last = dropChance[0]
                for (i in 1..<dropChance.size) {
                    last += dropChance[i]
                    assert(last <= 1.0)
                    dropChance[i] = last
                }
            }
        }
    }
}
