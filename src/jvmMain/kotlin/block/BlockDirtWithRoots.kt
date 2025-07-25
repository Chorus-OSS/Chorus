package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemBlock
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.particle.BoneMealParticle
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector3

class BlockDirtWithRoots(blockState: BlockState = properties.defaultState) : BlockDirt(blockState) {
    override val name: String
        get() = "Dirt With Roots"

    override val hardness: Double
        get() = 0.5

    override val resistance: Double
        get() = 2.5

    override fun onActivate(
        item: Item,
        player: Player?,
        blockFace: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float
    ): Boolean {
        val vector = Vector3(
            position.x, position.y - 1,
            position.z
        )
        if (!up().canBeReplaced()) {
            return false
        }
        if (item.isFertilizer && level.getBlock(vector).isAir) {
            if (player != null && (player.gamemode and 0x01) == 0) {
                item.count--
            }
            level.addParticle(BoneMealParticle(this.position))
            level.setBlock(vector, get(BlockID.HANGING_ROOTS))
            return true
        }
        if (item.isHoe) {
            vector.setY(position.y + 1)
            item.useOn(this)
            level.setBlock(this.position, get(BlockID.DIRT), true)
            level.dropItem(vector, ItemBlock(BlockHangingRoots.properties.defaultState, ""))
            if (player != null) {
                player.level!!.addSound(player.position, Sound.USE_GRASS)
            }
            return true
        } else if (item.isShovel) {
            item.useOn(this)
            level.setBlock(this.position, get(BlockID.GRASS_PATH))
            if (player != null) {
                player.level!!.addSound(player.position, Sound.USE_GRASS)
            }
            return true
        }

        return false
    }

    override fun canBeActivated(): Boolean {
        return true
    }

    override val toolType: Int
        get() = ItemTool.TYPE_SHOVEL

    override fun getDrops(item: Item): Array<Item> {
        return arrayOf(ItemBlock(BlockDirtWithRoots.properties.defaultState, ""))
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.DIRT_WITH_ROOTS)
    }
}
