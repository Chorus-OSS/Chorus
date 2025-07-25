package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.entity.effect.PotionType
import org.chorus_oss.chorus.event.player.PlayerItemConsumeEvent
import org.chorus_oss.chorus.level.vibration.VibrationEvent
import org.chorus_oss.chorus.level.vibration.VibrationType
import org.chorus_oss.chorus.math.Vector3

class ItemPotion @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    Item(ItemID.Companion.POTION, meta, count, "Potion") {
    init {
        updateName()
    }

    override var damage: Int
        get() = super.damage
        set(meta) {
            super.damage = (meta)
            updateName()
        }

    private fun updateName() {
        val potion = this.potion
        name = if (PotionType.WATER == potion) {
            buildName(potion, "Bottle", true)
        } else {
            buildName(potion!!, "Potion", true)
        }
    }

    override val maxStackSize: Int
        get() = 1

    override fun onClickAir(player: Player, directionVector: Vector3): Boolean {
        return true
    }

    override fun onUse(player: Player, ticksUsed: Int): Boolean {
        if (ticksUsed < 31) {
            return false
        }
        val consumeEvent = PlayerItemConsumeEvent(player, this)
        Server.instance.pluginManager.callEvent(consumeEvent)
        if (consumeEvent.cancelled) {
            return false
        }
        val potion = PotionType.get(this.damage)

        player.level!!.vibrationManager.callVibrationEvent(
            VibrationEvent(
                player,
                player.position.clone(),
                VibrationType.DRINKING
            )
        )

        if (player.isAdventure || player.isSurvival) {
            --this.count
            player.inventory.setItemInHand(this)
            player.inventory.addItem(ItemGlassBottle())
        }

        if (potion != null) {
            potion.applyEffects(player, false, 1.0)
        }
        return true
    }

    val potion: PotionType
        get() = PotionType.get(damage)

    companion object {
        fun buildName(potion: PotionType, type: String, includeLevel: Boolean): String {
            return when (potion.stringId) {
                "minecraft:water" -> "Water $type"
                "minecraft:mundane", "minecraft:long_mundane" -> "Mundane $type"
                "minecraft:thick" -> "Thick $type"
                "minecraft:awkward" -> "Awkward $type"
                "minecraft:turtle_master", "minecraft:long_turtle_master", "minecraft:strong_turtle_master" -> {
                    val name = "$type of the Turtle Master"
                    if (!includeLevel) {
                        name
                    }

                    if (potion.level <= 1) {
                        name
                    }

                    name + " " + potion.getRomanLevel()
                }

                else -> {
                    var finalName = potion.name
                    finalName = if (finalName!!.isEmpty()) {
                        type
                    } else {
                        "$type of $finalName"
                    }
                    if (includeLevel && potion.level > 1) {
                        finalName += " " + potion.getRomanLevel()
                    }
                    finalName
                }
            }
        }

        @JvmStatic
        fun fromPotion(potion: PotionType): ItemPotion {
            return ItemPotion(potion.id)
        }
    }
}
