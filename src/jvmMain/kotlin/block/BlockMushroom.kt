package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.BlockFlowerPot.FlowerPotBlock
import org.chorus_oss.chorus.event.level.StructureGrowEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.generator.`object`.BlockManager
import org.chorus_oss.chorus.level.generator.`object`.ObjectBigMushroom
import org.chorus_oss.chorus.level.particle.BoneMealParticle
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.utils.ChorusRandom
import java.util.concurrent.ThreadLocalRandom

abstract class BlockMushroom(blockState: BlockState) : BlockFlowable(blockState), FlowerPotBlock,
    Natural {
    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (!canStay()) {
                level.useBreakOn(this.position)

                return Level.BLOCK_UPDATE_NORMAL
            }
        }
        return 0
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
        if (canStay()) {
            level.setBlock(block.position, this, direct = true, update = true)
            return true
        }
        return false
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
        if (item.isFertilizer) {
            if (player != null && (player.gamemode and 0x01) == 0) {
                item.count--
            }

            if (ThreadLocalRandom.current().nextFloat() < 0.4) {
                this.grow()
            }

            level.addParticle(BoneMealParticle(this.position))
            return true
        }
        return false
    }

    fun grow(): Boolean {
        level.setBlock(this.position, get(BlockID.AIR), direct = true, update = false)

        val generator = ObjectBigMushroom(getType())

        val chunkManager = BlockManager(this.level)
        if (generator.generate(chunkManager, ChorusRandom(), this.position)) {
            val ev = StructureGrowEvent(this, chunkManager.blocks)
            Server.instance.pluginManager.callEvent(ev)
            if (ev.cancelled) {
                return false
            }
            for (block in ev.blockList) {
                level.setBlock(
                    Vector3(
                        block.position.floorX.toDouble(),
                        block.position.floorY.toDouble(),
                        block.position.floorZ.toDouble()
                    ), block
                )
            }
            return true
        } else {
            level.setBlock(this.position, this, direct = true, update = false)
            return false
        }
    }

    fun canStay(): Boolean {
        val block = this.down()
        return block.id == BlockID.MYCELIUM || block.id == BlockID.PODZOL || block is BlockNylium || (!block.isTransparent && level.getFullLight(
            this.position
        ) < 13)
    }

    override fun canSilkTouch(): Boolean {
        return true
    }

    override val toolType: Int
        get() = ItemTool.TYPE_AXE

    override val toolTier: Int
        get() = ItemTool.TIER_WOODEN

    protected abstract fun getType(): ObjectBigMushroom.MushroomType?

    override val isFertilizable: Boolean
        get() = true
}
