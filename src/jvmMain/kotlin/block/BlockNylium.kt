package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.generator.`object`.BlockManager
import org.chorus_oss.chorus.level.generator.`object`.ObjectNyliumVegetation
import org.chorus_oss.chorus.level.particle.BoneMealParticle
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.utils.ChorusRandom

abstract class BlockNylium(blockState: BlockState) : BlockSolid(blockState), Natural {
    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_RANDOM && !up().isTransparent) {
            level.setBlock(this.position, get(BlockID.NETHERRACK), false)
            return type
        }
        return 0
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
        val up = up()
        if (item.isNothing || !item.isFertilizer || !up.isAir) {
            return false
        }

        if (player != null && !player.isCreative) {
            item.count--
        }

        grow()

        level.addParticle(BoneMealParticle(up.position))

        return true
    }

    fun grow(): Boolean {
        val blockManager = BlockManager(this.level)
        ObjectNyliumVegetation.growVegetation(blockManager, this.position, ChorusRandom())
        blockManager.applySubChunkUpdate()
        return true
    }

    override val resistance: Double
        get() = 0.4

    override val hardness: Double
        get() = 0.4

    override fun getDrops(item: Item): Array<Item> {
        if (item.isPickaxe && item.tier >= ItemTool.TIER_WOODEN) {
            return arrayOf(Item.get(BlockID.NETHERRACK))
        }
        return Item.EMPTY_ARRAY
    }

    override fun canSilkTouch(): Boolean {
        return true
    }

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override val isFertilizable: Boolean
        get() = true
}
