package org.chorus_oss.chorus

import org.chorus_oss.chorus.entity.Attribute
import org.chorus_oss.chorus.entity.Attribute.Companion.getAttribute
import org.chorus_oss.chorus.entity.effect.EffectType
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause
import org.chorus_oss.chorus.event.entity.EntityRegainHealthEvent
import org.chorus_oss.chorus.event.player.PlayerFoodLevelChangeEvent
import org.chorus_oss.chorus.item.ItemFood
import kotlin.math.max
import kotlin.math.min

class PlayerFood(val player: Player, private var food: Int, private var saturation: Float) {
    val maxFood: Int = 20
    var exhaustion: Double = 0.0
        private set

    var foodTickTimer: Int = 0
        private set

    var isEnabled: Boolean = true
        get() = !(player.isCreative || player.isSpectator) && field

    fun getFood(): Int {
        return food
    }

    fun setFood(food: Int, saturation: Float) {
        var food1 = food
        food1 = max(0.0, min(food1.toDouble(), 20.0)).toInt()

        if (food1 <= 6 && this.food > 6 && player.isSprinting()) {
            player.setSprinting(false)
        }

        val event = PlayerFoodLevelChangeEvent(this.player, food1, saturation)
        Server.instance.pluginManager.callEvent(event)

        if (event.cancelled) {
            this.sendFood(this.food)
            return
        }

        this.food = event.foodLevel
        this.saturation = min(event.foodSaturationLevel.toDouble(), food1.toDouble()).toFloat()

        this.sendFood()
    }

    fun getSaturation(): Float {
        return saturation
    }

    fun setSaturation(saturation: Float) {
        var saturation1 = saturation
        saturation1 = max(0.0, min(saturation1.toDouble(), food.toDouble())).toFloat()

        val event = PlayerFoodLevelChangeEvent(player, food, saturation1)
        Server.instance.pluginManager.callEvent(event)

        if (!event.cancelled) {
            this.saturation = event.foodSaturationLevel
        }
    }

    fun addFood(food: ItemFood) {
        this.addFood(food.foodRestore, food.saturationRestore)
    }

    fun setFood(food: Int) {
        this.setFood(food, -1f)
    }

    fun addFood(food: Int, saturation: Float) {
        this.setFood(this.food + food, this.saturation + saturation)
    }

    @JvmOverloads
    fun sendFood(food: Int = this.food) {
        if (player.spawned) {
            val attribute: Attribute =
                player.getAttributes().computeIfAbsent(Attribute.FOOD) { obj: Int -> getAttribute(obj) }
            if (attribute.getValue() != food.toFloat()) {
                attribute.setValue(food.toFloat())
                player.syncAttribute(attribute)
            }
        }
    }

    val isHungry: Boolean
        get() = food < maxFood

    fun setExhaustion(exhaustion: Float) {
        var exhaustion1 = exhaustion
        while (exhaustion1 >= 4.0f) {
            exhaustion1 -= 4.0f
            var saturation = this.saturation
            if (saturation > 0) {
                saturation = max(0.0, (saturation - 1.0f).toDouble()).toFloat()
                this.setSaturation(saturation)
            } else {
                var food = this.food
                if (food > 0) {
                    food--
                    this.setFood(max(food.toDouble(), 0.0).toInt())
                }
            }
        }
        this.exhaustion = exhaustion1.toDouble()
    }

    fun exhaust(amount: Double) {
        if (!this.isEnabled || Server.instance
                .getDifficulty() == 0 || player.hasEffect(EffectType.SATURATION)
        ) {
            return
        }

        var exhaustion = this.exhaustion + amount

        while (exhaustion >= 4.0f) {
            exhaustion -= 4.0

            var saturation = this.saturation
            if (saturation > 0) {
                saturation = max(0.0, (saturation - 1.0f).toDouble()).toFloat()
                this.setSaturation(saturation)
            } else {
                var food = this.food
                if (food > 0) {
                    food--
                    this.setFood(max(food.toDouble(), 0.0).toInt())
                }
            }
        }

        this.exhaustion = exhaustion
    }

    fun reset() {
        this.food = 20
        this.saturation = 20f
        this.exhaustion = 0.0
        this.foodTickTimer = 0
        this.sendFood()
    }

    fun tick(tickDiff: Int) {
        if (!player.isAlive() || !this.isEnabled) {
            return
        }

        val health = player.health.toDouble()

        this.foodTickTimer += tickDiff
        if (this.foodTickTimer >= 80) {
            this.foodTickTimer = 0
        }

        val difficulty: Int = Server.instance.getDifficulty()

        if (difficulty == 0 && this.foodTickTimer % 10 == 0) {
            if (this.isHungry) {
                this.addFood(1, 0f)
            }
            if (this.foodTickTimer % 20 == 0 && health < player.getMaxHealth()) {
                player.heal(EntityRegainHealthEvent(this.player, 1f, EntityRegainHealthEvent.CAUSE_EATING))
            }
        }

        if (this.foodTickTimer == 0) {
            if (this.food >= 18) {
                if (health < player.getMaxHealth()) {
                    player.heal(EntityRegainHealthEvent(this.player, 1f, EntityRegainHealthEvent.CAUSE_EATING))
                    this.exhaust(6.0)
                }
            } else if (food <= 0) {
                if ((difficulty == 1 && health > 10) || (difficulty == 2 && health > 1) || difficulty == 3) {
                    player.attack(EntityDamageEvent(this.player, DamageCause.HUNGER, 1f))
                }
            }
        }

        if (this.food <= 6) {
            player.setSprinting(false)
        }
    }
}
