package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockBeehive
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.item.EntityAreaEffectCloud
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.vibration.VibrationEvent
import org.chorus_oss.chorus.level.vibration.VibrationType
import org.chorus_oss.chorus.math.BlockFace


class ItemGlassBottle @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    Item(ItemID.Companion.GLASS_BOTTLE, meta, count, "Glass Bottle") {
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
        var filled: Item? = null
        if (level.getCollidingEntities(player.getBoundingBox().grow(1.1, 1.1, 1.1))
                .any { entity: Entity -> entity is EntityAreaEffectCloud && entity.isDragonBreath() }
        ) {
            filled = ItemDragonBreath()

            level.getCollidingEntities(
                player.getBoundingBox().grow(1.1, 1.1, 1.1)
            ).firstOrNull { entity: Entity -> entity is EntityAreaEffectCloud && entity.isDragonBreath() }
                .also { entity ->
                    if (entity is EntityAreaEffectCloud) {
                        entity.setRadius(
                            entity.getRadius() - 1,
                            true
                        )
                    }
                }
        } else if (target.id == BlockID.FLOWING_WATER || target.id == BlockID.WATER) {
            filled = ItemPotion()
        } else if (target is BlockBeehive && target.isFull) {
            filled = get(ItemID.Companion.HONEY_BOTTLE)
            target.honeyCollected(player)
            level.addSound(player.position, Sound.BUCKET_FILL_WATER)
        }

        if (filled != null) {
            if (this.count == 1) {
                player.inventory.setItemInHand(filled)
            } else if (this.count > 1) {
                count--
                player.inventory.setItemInHand(this)
                if (player.inventory.canAddItem(filled)) {
                    player.inventory.addItem(filled)
                } else {
                    player.level!!.dropItem(
                        player.position.add(0.0, 1.3, 0.0),
                        filled,
                        player.getDirectionVector().multiply(0.4)
                    )
                }
            }

            level.vibrationManager.callVibrationEvent(
                VibrationEvent(
                    player,
                    target.position.add(0.5, 0.5, 0.5),
                    VibrationType.FLUID_PICKUP
                )
            )
        }

        return false
    }
}
