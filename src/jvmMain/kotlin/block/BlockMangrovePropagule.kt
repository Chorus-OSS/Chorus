package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.BlockFlower.Companion.isSupportValid
import org.chorus_oss.chorus.block.BlockFlowerPot.FlowerPotBlock
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.BooleanPropertyType
import org.chorus_oss.chorus.event.level.StructureGrowEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.generator.`object`.BlockManager
import org.chorus_oss.chorus.level.generator.`object`.ObjectMangroveTree
import org.chorus_oss.chorus.level.particle.BoneMealParticle
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.utils.ChorusRandom
import java.util.concurrent.ThreadLocalRandom

class BlockMangrovePropagule @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockFlowable(blockstate), FlowerPotBlock {
    override val name: String
        get() = "Mangrove Propaugle"

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
        // TODO: 实现红树树苗放置逻辑
        if (isSupportValid(block.down())) {
            level.setBlock(block.position, this, true, true)
            return true
        }

        return false
    }

    val isHanging: Boolean
        get() = getPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.HANGING)

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
        if (item.isFertilizer) { // BoneMeal
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

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (!isSupportValid(down())) {
                level.useBreakOn(this.position)
                return Level.BLOCK_UPDATE_NORMAL
            }
        } else if (type == Level.BLOCK_UPDATE_RANDOM) { //Growth
            this.grow()
        }
        return Level.BLOCK_UPDATE_NORMAL
    }

    protected fun grow() {
        val chunkManager = BlockManager(this.level)
        val vector3 = Vector3(
            position.x, position.y - 1,
            position.z
        )
        val objectMangroveTree = ObjectMangroveTree()
        objectMangroveTree.generate(chunkManager, ChorusRandom(), this.position)
        val ev = StructureGrowEvent(this, chunkManager.blocks)
        Server.instance.pluginManager.callEvent(ev)
        if (ev.cancelled) {
            return
        }
        chunkManager.applySubChunkUpdate(ev.blockList)
        level.setBlock(this.position, get(BlockID.AIR))
        if (level.getBlock(vector3).id == BlockID.DIRT_WITH_ROOTS) {
            level.setBlock(vector3, get(BlockID.DIRT))
        }
        for (block in ev.blockList) {
            if (block.isAir) continue
            level.setBlock(block.position, block)
        }
    }

    override val waterloggingLevel: Int
        get() = 1

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.MANGROVE_PROPAGULE,
            CommonBlockProperties.HANGING,
            CommonBlockProperties.PROPAGULE_STAGE
        )
    }
}
