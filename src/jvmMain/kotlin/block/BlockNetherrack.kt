package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.math.BlockFace
import java.util.concurrent.ThreadLocalRandom

class BlockNetherrack(blockState: BlockState = properties.defaultState) : BlockSolid(blockState) {
    override val resistance: Double
        get() = 0.4

    override val hardness: Double
        get() = 0.4

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override val name: String
        get() = "Netherrack"

    override val toolTier: Int
        get() = ItemTool.TIER_WOODEN

    override fun onActivate(
        item: Item,
        player: Player?,
        blockFace: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float
    ): Boolean {
        if (item.isNothing || !item.isFertilizer || up().id !== BlockID.AIR) {
            return false
        }

        val options: MutableList<String> = ArrayList()
        for (face in BlockFace.Plane.HORIZONTAL_FACES) {
            val id = getSide(face).id
            if ((id == BlockID.CRIMSON_NYLIUM || id == BlockID.WARPED_NYLIUM) && !options.contains(id)) {
                options.add(id)
            }
        }

        val nylium: String
        val size = options.size
        nylium = if (size == 0) {
            return false
        } else if (size == 1) {
            options[0]
        } else {
            options[ThreadLocalRandom.current().nextInt(size)]
        }

        if (level.setBlock(this.position, get(nylium), true)) {
            if (player == null || !player.isCreative) {
                item.count--
            }
            return true
        }

        return false
    }

    override fun canBeActivated(): Boolean {
        return true
    }

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override val isFertilizable: Boolean
        get() = true

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.NETHERRACK)
    }
}
