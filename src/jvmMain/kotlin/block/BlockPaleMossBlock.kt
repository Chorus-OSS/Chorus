package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Locator
import java.util.*
import kotlin.math.abs

class BlockPaleMossBlock @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockMossBlock(blockstate) {
    override val name: String
        get() = "Pale Moss"


    override fun convertToMoss(pos: Locator) {
        val random = Random()
        var x = pos.position.x - 3
        while (x <= pos.position.x + 3) {
            var z = pos.position.z - 3
            while (z <= pos.position.z + 3) {
                var y = pos.position.y + 5
                while (y >= pos.position.y - 5) {
                    if (canConvertToMoss(
                            pos.level.getBlock(
                                Locator(
                                    x,
                                    y,
                                    z,
                                    pos.level
                                ).position
                            )
                        ) && (random.nextDouble() < 0.6 || abs(x - pos.position.x) < 3 && abs(z - pos.position.z) < 3)
                    ) {
                        pos.level.setBlock(Locator(x, y, z, pos.level).position, get(BlockID.PALE_MOSS_BLOCK))
                        break
                    }
                    y--
                }
                z++
            }
            x++
        }
    }

    override fun populateRegion(pos: Locator) {
        val random = Random()
        var x = pos.position.x - 3
        while (x <= pos.position.x + 3) {
            var z = pos.position.z - 3
            while (z <= pos.position.z + 3) {
                var y = pos.position.y + 5
                while (y >= pos.position.y - 5) {
                    if (canBePopulated(Locator(x, y, z, pos.level))) {
                        if (!canGrowPlant(Locator(x, y, z, pos.level))) break
                        val randomDouble = random.nextDouble()
                        if (randomDouble >= 0 && randomDouble < 0.3125) {
                            pos.level.setBlock(
                                Locator(x, y, z, pos.level).position,
                                get(BlockID.TALL_GRASS),
                                true,
                                true
                            )
                        }
                        if (randomDouble >= 0.3125 && randomDouble < 0.46875) {
                            pos.level.setBlock(
                                Locator(x, y, z, pos.level).position,
                                get(BlockID.PALE_MOSS_CARPET),
                                true,
                                true
                            )
                        }
                        if (randomDouble >= 0.46875 && randomDouble < 0.53125) {
                            if (canBePopulated2BlockAir(Locator(x, y, z, pos.level))) {
                                val rootBlock = BlockLargeFern()
                                rootBlock.isTopHalf = false
                                pos.level.setBlock(Locator(x, y, z, pos.level).position, rootBlock, true, true)
                                val topBlock = BlockLargeFern()
                                topBlock.isTopHalf = true
                                pos.level.setBlock(Locator(x, y + 1, z, pos.level).position, topBlock, true, true)
                            } else {
                                val block = BlockTallGrass()
                                pos.level.setBlock(Locator(x, y, z, pos.level).position, block, true, true)
                            }
                        }
                        break
                    }
                    y--
                }
                z++
            }
            x++
        }
    }

    override fun getDrops(item: Item): Array<Item> {
        return arrayOf(super.toItem())
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.PALE_MOSS_BLOCK)

    }
}