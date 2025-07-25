package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.level.ParticleEffect
import org.chorus_oss.chorus.math.BlockFace
import java.util.*
import kotlin.math.abs

open class BlockMossBlock @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockSolid(blockstate), Natural {
    override val name: String
        get() = "Moss"

    override val hardness: Double
        get() = 0.1

    override val resistance: Double
        get() = 2.5

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
        if (item.isFertilizer && blockFace == BlockFace.UP) {
            convertToMoss(this)
            populateRegion(this)
            level.addParticleEffect(position.add(0.5, 1.5, 0.5), ParticleEffect.CROP_GROWTH_AREA)
            item.count--
            return true
        }
        return false
    }

    fun canConvertToMoss(block: Block): Boolean {
        val id = block.id
        return id == BlockID.GRASS_BLOCK ||
                id == BlockID.DIRT ||
                id == BlockID.DIRT_WITH_ROOTS ||
                id == BlockID.STONE ||
                id == BlockID.MYCELIUM ||
                id == BlockID.DEEPSLATE ||
                id == BlockID.TUFF
    }

    fun canBePopulated(pos: Locator): Boolean {
        return pos.add(0.0, -1.0, 0.0).levelBlock.isSolid && (pos.add(
            0.0,
            -1.0,
            0.0
        ).levelBlock.id != BlockID.MOSS_CARPET) && pos.levelBlock.id === BlockID.AIR
    }

    fun canBePopulated2BlockAir(pos: Locator): Boolean {
        return pos.add(0.0, -1.0, 0.0).levelBlock.isSolid && (pos.add(
            0.0,
            -1.0,
            0.0
        ).levelBlock.id != BlockID.MOSS_CARPET) && pos.levelBlock.id === BlockID.AIR && pos.add(
            0.0,
            1.0,
            0.0
        ).levelBlock.id === BlockID.AIR
    }

    open fun convertToMoss(pos: Locator) {
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
                        pos.level.setBlock(Locator(x, y, z, pos.level).position, get(BlockID.MOSS_BLOCK))
                        break
                    }
                    y--
                }
                z++
            }
            x++
        }
    }

    open fun populateRegion(pos: Locator) {
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
                                get(BlockID.MOSS_CARPET),
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
                        if (randomDouble >= 0.53125 && randomDouble < 0.575) {
                            pos.level.setBlock(Locator(x, y, z, pos.level).position, get(BlockID.AZALEA), true, true)
                        }
                        if (randomDouble >= 0.575 && randomDouble < 0.6) {
                            pos.level.setBlock(
                                Locator(x, y, z, pos.level).position,
                                get(BlockID.FLOWERING_AZALEA),
                                true,
                                true
                            )
                        }
                        if (randomDouble >= 0.6 && randomDouble < 1) {
                            pos.level.setBlock(Locator(x, y, z, pos.level).position, get(BlockID.AIR), true, true)
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

    fun canGrowPlant(pos: Locator): Boolean {
        return when (pos.add(0.0, -1.0, 0.0).levelBlock.id) {
            BlockID.GRASS_BLOCK, BlockID.DIRT, BlockID.PODZOL, BlockID.FARMLAND, BlockID.MYCELIUM, BlockID.DIRT_WITH_ROOTS, BlockID.MOSS_BLOCK, BlockID.PALE_MOSS_BLOCK -> true
            else -> false
        }
    }

    override val toolType: Int
        get() = ItemTool.TYPE_HOE

    override fun getDrops(item: Item): Array<Item> {
        return arrayOf(super.toItem())
    }

    override val isFertilizable: Boolean
        get() = true

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.MOSS_BLOCK)

    }
}