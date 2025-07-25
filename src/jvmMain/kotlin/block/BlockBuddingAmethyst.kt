package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.BlockFace.Companion.fromIndex
import org.chorus_oss.chorus.utils.ChorusRandom

class BlockBuddingAmethyst(blockState: BlockState = properties.defaultState) : BlockSolid(blockState) {
    override val name: String
        get() = "Budding Amethyst"

    override val resistance: Double
        get() = 1.5

    override val hardness: Double
        get() = 1.5

    override fun breaksWhenMoved(): Boolean {
        return true
    }

    override fun sticksToPiston(): Boolean {
        return false
    }

    override fun getDrops(item: Item): Array<Item> {
        return Item.EMPTY_ARRAY
    }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_RANDOM) {
            if (RANDOM.nextInt(5) == 1) {
                tryGrow(0)
            }
            return type
        }
        return 0
    }

    fun tryGrow(time: Int) {
        if (time > 6) {
            return
        }
        val face = fromIndex(RANDOM.nextInt(6))
        val side = this.getSide(face)
        val tmp: BlockAmethystBud
        if (side.canBeReplaced()) {
            tmp = BlockSmallAmethystBud()
            tmp.blockFace = face
            level.setBlock(side.position, tmp, direct = true, update = true)
        } else if (side is BlockSmallAmethystBud) {
            tmp = BlockMediumAmethystBud()
            tmp.blockFace = face
            level.setBlock(side.position, tmp, direct = true, update = true)
        } else if (side is BlockMediumAmethystBud) {
            tmp = BlockLargeAmethystBud()
            tmp.blockFace = face
            level.setBlock(side.position, tmp, direct = true, update = true)
        } else if (side is BlockLargeAmethystBud) {
            tmp = BlockAmethystCluster()
            tmp.blockFace = face
            level.setBlock(side.position, tmp, direct = true, update = true)
        } else {
            tryGrow(time + 1)
        }
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.BUDDING_AMETHYST)

        private val RANDOM: ChorusRandom = ChorusRandom()
    }
}
