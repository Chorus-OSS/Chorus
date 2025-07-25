package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockCaveVines
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.math.BlockFace
import java.util.concurrent.ThreadLocalRandom

class ItemGlowBerries @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) :
    ItemFood(ItemID.Companion.GLOW_BERRIES, 0, count, "Glow Berries") {
    override fun canBeActivated(): Boolean {
        return true
    }

    override fun onActivate(
        level: Level,
        player: Player,
        block: Block,
        target: Block,
        face: BlockFace,
        fx: Double,
        fy: Double,
        fz: Double
    ): Boolean {
        if (BlockCaveVines.isValidSupport(block) && face == BlockFace.DOWN) {
            val tmp = BlockCaveVines()
            tmp.setPropertyValue(CommonBlockProperties.GROWING_PLANT_AGE, ThreadLocalRandom.current().nextInt(26))
            level.setBlock(target.down().position, tmp)
            level.addSound(target.down().position, Sound.DIG_CAVE_VINES)
            if (player.isAdventure || player.isSurvival) {
                --this.count
                player.inventory.setItemInHand(this)
            }
            return true
        }

        return false
    }

    override val foodRestore: Int
        get() = 2

    override val saturationRestore: Float
        get() = 0.4f
}
