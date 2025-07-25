package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockFire
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.event.block.BlockIgniteEvent
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.vibration.VibrationEvent
import org.chorus_oss.chorus.level.vibration.VibrationType
import org.chorus_oss.chorus.math.BlockFace
import java.util.concurrent.ThreadLocalRandom

class ItemFlintAndSteel @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemTool(ItemID.Companion.FLINT_AND_STEEL, meta, count, "Flint and Steel") {
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
        if (player.isAdventure) {
            return false
        }

        if (block.isAir && target.burnChance != -1 && (target.isSolid || target.burnChance > 0)) {
            if (target.id == BlockID.OBSIDIAN) {
                if (level.dimension != Level.DIMENSION_THE_END) {
                    if (level.createPortal(target)) {
                        damageItem(player, block)
                        return true
                    }
                }
            }

            val fire = Block.get(BlockID.FIRE) as BlockFire
            fire.position.x = block.position.x
            fire.position.y = block.position.y
            fire.position.z = block.position.z
            fire.level = level

            if (fire.isBlockTopFacingSurfaceSolid(fire.down()) || fire.canNeighborBurn()) {
                val e = BlockIgniteEvent(block, null, player, BlockIgniteEvent.BlockIgniteCause.FLINT_AND_STEEL)
                Server.instance.pluginManager.callEvent(e)

                if (!e.cancelled) {
                    level.setBlock(fire.position, fire, true)
                    level.scheduleUpdate(fire, fire.tickRate() + ThreadLocalRandom.current().nextInt(10))
                }
                damageItem(player, block)
                return true
            }

            damageItem(player, block)
            return true
        }
        damageItem(player, block)
        return false
    }

    private fun damageItem(player: Player, block: Block) {
        if (!player.isCreative && useOn(block)) {
            if (this.damage >= this.maxDurability) {
                this.count = 0
                player.inventory.setItemInHand(AIR)
            } else {
                player.inventory.setItemInHand(this)
            }
        }
        block.level.addSound(block.position, Sound.FIRE_IGNITE)
    }

    override val maxDurability: Int
        get() = DURABILITY_FLINT_STEEL

    override fun useOn(block: Block): Boolean {
        // TODO: initiator should be an entity who use it but not null
        block.level.vibrationManager.callVibrationEvent(
            VibrationEvent(
                null,
                block.position.add(0.5, 0.5, 0.5),
                VibrationType.BLOCK_PLACE
            )
        )
        return super.useOn(block)
    }
}