package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.event.player.PlayerItemConsumeEvent
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.vibration.VibrationEvent
import org.chorus_oss.chorus.level.vibration.VibrationType
import org.chorus_oss.chorus.math.Vector3

class ItemMilkBucket : ItemBucket(ItemID.Companion.MILK_BUCKET) {
    override var damage: Int
        get() = super.damage
        set(meta) {
        }

    override fun onUse(player: Player, ticksUsed: Int): Boolean {
        if (ticksUsed < 31) {
            return false
        }

        val event = PlayerItemConsumeEvent(player, this)
        Server.instance.pluginManager.callEvent(event)

        if (event.cancelled) {
            player.inventory.sendContents(player)
            return false
        }

        player.removeAllEffects()

        player.completeUsingItem(
            this.runtimeId.toShort(),
            org.chorus_oss.protocol.packets.CompletedUsingItemPacket.Companion.ItemUseMethod.Eat
        )

        if (player.isAdventure || player.isSurvival) {
            --this.count
            player.inventory.addItem(get(ItemID.Companion.BUCKET, 0, 1))
            player.inventory.setItemInHand(this)
            player.level!!.addSound(player.position, Sound.RANDOM_BURP)
        }

        player.level!!.vibrationManager.callVibrationEvent(
            VibrationEvent(
                player,
                player.position.add(0.0, player.getEyeHeight().toDouble()),
                VibrationType.DRINKING
            )
        )

        return true
    }

    override fun onClickAir(player: Player, directionVector: Vector3): Boolean {
        return true
    }
}