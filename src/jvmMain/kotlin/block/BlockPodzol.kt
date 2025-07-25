package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.math.BlockFace

class BlockPodzol(blockState: BlockState = properties.defaultState) : BlockDirt(blockState) {
    override val name: String
        get() = "Podzol"

    override fun canSilkTouch(): Boolean {
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
        if (!up().canBeReplaced()) {
            return false
        }

        if (item.isShovel) {
            item.useOn(this)
            level.setBlock(this.position, get(BlockID.GRASS_PATH))
            if (player != null) {
                player.level!!.addSound(player.position, Sound.USE_GRASS)
            }
            return true
        }
        return false
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.PODZOL)

    }
}
