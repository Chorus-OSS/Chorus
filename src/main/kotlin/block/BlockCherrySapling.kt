package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.BlockFlowerPot.FlowerPotBlock
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.WoodType
import org.chorus_oss.chorus.event.level.StructureGrowEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.generator.`object`.BlockManager
import org.chorus_oss.chorus.level.generator.`object`.ObjectCherryTree
import org.chorus_oss.chorus.level.particle.BoneMealParticle
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.utils.ChorusRandom
import java.util.concurrent.ThreadLocalRandom
import java.util.function.Predicate

class BlockCherrySapling @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockSapling(blockState), FlowerPotBlock {
    override fun getWoodType(): WoodType {
        return WoodType.CHERRY
    }

    override val name: String
        get() = "Cherry Sapling"

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (isSupportInvalid) {
                level.useBreakOn(this.position)
                return Level.BLOCK_UPDATE_NORMAL
            }
        } else if (type == Level.BLOCK_UPDATE_RANDOM) { //Growth
            if (level.getFullLight(position.add(0.0, 1.0, 0.0)) >= BlockCrops.MIN_LIGHT_LEVEL) {
                if (isAged) {
                    this.grow()
                } else {
                    isAged = true
                    level.setBlock(this.position, this, true)
                    return Level.BLOCK_UPDATE_RANDOM
                }
            } else {
                return Level.BLOCK_UPDATE_RANDOM
            }
        }
        return Level.BLOCK_UPDATE_NORMAL
    }

    private fun grow() {
        val blockManager = BlockManager(this.level)
        val vector3 = Vector3(
            position.x, position.y - 1,
            position.z
        )
        val objectCherryTree = ObjectCherryTree()
        val generate: Boolean = objectCherryTree.generate(blockManager, ChorusRandom.create(), this.position)
        if (generate) {
            val ev = StructureGrowEvent(this, blockManager.blocks)
            Server.instance.pluginManager.callEvent(ev)
            if (ev.cancelled) {
                return
            }
            if (level.getBlock(vector3).id == BlockID.DIRT_WITH_ROOTS) {
                level.setBlock(vector3, get(BlockID.DIRT))
            }
            blockManager.applySubChunkUpdate(
                ev.blockList
            ) { block: Block -> !block.isAir }
        }
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
        if (isSupportInvalid) {
            return false
        }

        if (levelBlock is BlockLiquid || getLevelBlockAtLayer(1) is BlockLiquid) {
            return false
        }

        level.setBlock(this.position, this, direct = true, update = true)
        return true
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
        if (item.isFertilizer) { // BoneMeal and so on
            if (player != null && !player.isCreative) {
                item.count--
            }

            level.addParticle(BoneMealParticle(this.position))
            if (ThreadLocalRandom.current().nextFloat() >= 0.45) {
                return true
            }

            this.grow()

            return true
        }
        return false
    }

    private val isSupportInvalid: Boolean
        get() {
            val downId = down().id
            return !(downId == BlockID.DIRT || downId == BlockID.GRASS_BLOCK || downId == BlockID.SAND || downId == BlockID.GRAVEL || downId == BlockID.PODZOL)
        }

    var isAge: Boolean
        get() = this.getPropertyValue(CommonBlockProperties.AGE_BIT)
        set(age) {
            this.setPropertyValue(
                CommonBlockProperties.AGE_BIT,
                age
            )
        }

    override val isFertilizable: Boolean
        get() = true

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.CHERRY_SAPLING, CommonBlockProperties.AGE_BIT)
    }
}
