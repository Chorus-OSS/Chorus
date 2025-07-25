package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.BlockFlowerPot.FlowerPotBlock
import org.chorus_oss.chorus.event.level.StructureGrowEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.generator.`object`.BlockManager
import org.chorus_oss.chorus.level.generator.`object`.ObjectAzaleaTree
import org.chorus_oss.chorus.level.generator.`object`.ObjectGenerator
import org.chorus_oss.chorus.level.particle.BoneMealParticle
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.utils.ChorusRandom
import java.util.concurrent.ThreadLocalRandom

open class BlockAzalea @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockSolid(blockstate), FlowerPotBlock {
    override val name: String
        get() = "Azalea"

    override val waterloggingLevel: Int
        get() = 1

    override val hardness: Double
        get() = 0.0

    override val resistance: Double
        get() = 0.0

    override val isFertilizable: Boolean
        get() = true

    override fun onActivate(
        item: Item,
        player: Player?,
        blockFace: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float
    ): Boolean {
        if (item.isFertilizer) { // BoneMeal
            if (player != null && !player.isCreative) {
                item.count--
            }

            level.addParticle(BoneMealParticle(this.position))
            if (ThreadLocalRandom.current().nextInt(4) == 0) {
                this.grow()
                return true
            }
        }
        return false
    }

    override fun onUpdate(type: Int): Int {
        val chance = ThreadLocalRandom.current().nextDouble(1.0)
        val aged = chance > 0.8
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            val down = down()
            if (down != null && !BlockFlower.isSupportValid(down)) {
                level.useBreakOn(this.position)
                return Level.BLOCK_UPDATE_NORMAL
            }
        } else if (type == Level.BLOCK_UPDATE_RANDOM) { //Growth
            if (ThreadLocalRandom.current().nextInt(1, 8) == 1 && level.getFullLight(
                    position.add(0.0, 1.0, 0.0)
                ) >= BlockCrops.MIN_LIGHT_LEVEL
            ) {
                if (aged) {
                    this.grow()
                } else {
                    level.setBlock(this.position, this, true)
                    return Level.BLOCK_UPDATE_RANDOM
                }
            } else {
                return Level.BLOCK_UPDATE_RANDOM
            }
        }
        return Level.BLOCK_UPDATE_NORMAL
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
        val down = down()
        if (down != null && BlockFlower.isSupportValid(down)) {
            level.setBlock(block.position, this, direct = true, update = true)
            return true
        }

        return false
    }

    override fun isSolid(side: BlockFace): Boolean {
        return false
    }

    override fun getDrops(item: Item): Array<Item> {
        return arrayOf(toItem())
    }

    override fun canBeActivated(): Boolean {
        return true
    }

    fun isSameType(pos: Vector3): Boolean {
        val block = level.getBlock(pos)
        return block.id == this.id && block.properties === this.properties
    }

    private fun grow() {

        val generator: ObjectGenerator = ObjectAzaleaTree()
        val vector3 = position.add(0.0, 0.0, 0.0)

        val chunkManager = BlockManager(this.level)
        val success: Boolean = generator.generate(chunkManager, ChorusRandom.create(), vector3)
        val ev = StructureGrowEvent(this, chunkManager.blocks)
        Server.instance.pluginManager.callEvent(ev)
        if (ev.cancelled || !success) {
            return
        }

        for (block in ev.blockList) {
            level.setBlock(block.position, block)
        }

        level.setBlock(this.position, get(BlockID.OAK_LOG))
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.AZALEA)
    }
}
