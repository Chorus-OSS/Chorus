package org.chorus_oss.chorus.entity.effect

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.potion.PotionApplyEvent
import org.chorus_oss.chorus.registry.Registries

data class PotionType(
    @JvmField val name: String?,
    @JvmField val stringId: String,
    @JvmField val id: Int,
    @JvmField val level: Int,
    val effects: PotionEffects
) {
    constructor(name: String?, stringId: String, id: Int, effects: PotionEffects) : this(name, stringId, id, 1, effects)

    fun getEffects(splash: Boolean): List<Effect> {
        return effects.getEffects(splash)
    }

    fun applyEffects(entity: Entity, splash: Boolean, health: Double) {
        val event: PotionApplyEvent = PotionApplyEvent(this, this.getEffects(splash), entity)
        Server.instance.pluginManager.callEvent(event)

        if (event.cancelled) {
            return
        }

        for (effect: Effect in event.applyEffects) {
            val duration: Int = ((if (splash) health else 1.0) * effect.getDuration().toDouble() + 0.5).toInt()

            effect.setDuration(duration)
            entity.addEffect(effect)
        }
    }

    fun getRomanLevel(): String {
        var currentLevel: Int = this.level
        if (currentLevel == 0) {
            return "0"
        }

        val sb: StringBuilder = StringBuilder(4)
        if (currentLevel < 0) {
            sb.append('-')
            currentLevel *= -1
        }

        appendRoman(sb, currentLevel)
        return sb.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (other is PotionType) {
            return other.stringId == this.stringId && other.id == this.id
        }
        return false
    }

    companion object {
        @JvmField
        val WATER: PotionType = PotionType("Water", "minecraft:water", 0, PotionEffects.EMPTY)

        @JvmField
        val MUNDANE: PotionType = PotionType("Mundane", "minecraft:mundane", 1, PotionEffects.EMPTY)

        @JvmField
        val MUNDANE_LONG: PotionType =
            PotionType("Long Mundane", "minecraft:long_mundane", 2, PotionEffects.EMPTY)

        @JvmField
        val THICK: PotionType = PotionType("Thick", "minecraft:thick", 3, PotionEffects.EMPTY)

        @JvmField
        val AWKWARD: PotionType = PotionType("Awkward", "minecraft:awkward", 4, PotionEffects.EMPTY)

        @JvmField
        val NIGHT_VISION: PotionType =
            PotionType("Night Vision", "minecraft:nightvision", 5, PotionEffects.NIGHT_VISION)

        @JvmField
        val NIGHT_VISION_LONG: PotionType =
            PotionType("Long Night Vision", "minecraft:long_nightvision", 6, PotionEffects.NIGHT_VISION_LONG)

        @JvmField
        val INVISIBILITY: PotionType =
            PotionType("Invisibility", "minecraft:invisibility", 7, PotionEffects.INVISIBILITY)

        @JvmField
        val INVISIBILITY_LONG: PotionType =
            PotionType("Long Invisibility", "minecraft:long_invisibility", 8, PotionEffects.INVISIBILITY_LONG)

        @JvmField
        val LEAPING: PotionType = PotionType("Leaping", "minecraft:leaping", 9, PotionEffects.LEAPING)

        @JvmField
        val LEAPING_LONG: PotionType =
            PotionType("Long Leaping", "minecraft:long_leaping", 10, PotionEffects.LEAPING_LONG)

        @JvmField
        val LEAPING_STRONG: PotionType =
            PotionType("Strong Leaping", "minecraft:strong_leaping", 11, 2, PotionEffects.LEAPING_STRONG)

        @JvmField
        val FIRE_RESISTANCE: PotionType =
            PotionType("Fire Resistance", "minecraft:fire_resistance", 12, PotionEffects.FIRE_RESISTANCE)

        @JvmField
        val FIRE_RESISTANCE_LONG: PotionType = PotionType(
            "Long Fire Resistance",
            "minecraft:long_fire_resistance",
            13,
            PotionEffects.FIRE_RESISTANCE_LONG
        )

        @JvmField
        val SWIFTNESS: PotionType =
            PotionType("Swiftness", "minecraft:swiftness", 14, PotionEffects.SWIFTNESS)

        @JvmField
        val SWIFTNESS_LONG: PotionType =
            PotionType("Long Swiftness", "minecraft:long_swiftness", 15, PotionEffects.SWIFTNESS_LONG)

        @JvmField
        val SWIFTNESS_STRONG: PotionType = PotionType(
            "Strong Swiftness",
            "minecraft:strong_swiftness",
            16,
            2,
            PotionEffects.SWIFTNESS_STRONG
        )

        @JvmField
        val SLOWNESS: PotionType = PotionType("Slowness", "minecraft:slowness", 17, PotionEffects.SLOWNESS)

        @JvmField
        val SLOWNESS_LONG: PotionType =
            PotionType("Long Slowness", "minecraft:long_slowness", 18, PotionEffects.SLOWNESS_LONG)

        @JvmField
        val WATER_BREATHING: PotionType =
            PotionType("Water Breathing", "minecraft:water_breathing", 19, PotionEffects.WATER_BREATHING)

        @JvmField
        val WATER_BREATHING_LONG: PotionType = PotionType(
            "Long Water Breathing",
            "minecraft:long_water_breathing",
            20,
            PotionEffects.WATER_BREATHING_LONG
        )

        @JvmField
        val HEALING: PotionType = PotionType("Healing", "minecraft:healing", 21, PotionEffects.HEALING)

        @JvmField
        val HEALING_STRONG: PotionType =
            PotionType("Strong Healing", "minecraft:strong_healing", 22, 2, PotionEffects.HEALING_STRONG)

        @JvmField
        val HARMING: PotionType = PotionType("Harming", "minecraft:harming", 23, PotionEffects.HARMING)

        @JvmField
        val HARMING_STRONG: PotionType =
            PotionType("Strong Harming", "minecraft:strong_harming", 24, 2, PotionEffects.HARMING_STRONG)

        @JvmField
        val POISON: PotionType = PotionType("Poison", "minecraft:poison", 25, PotionEffects.POISON)

        @JvmField
        val POISON_LONG: PotionType =
            PotionType("Long Poison", "minecraft:long_poison", 26, PotionEffects.POISON_LONG)

        @JvmField
        val POISON_STRONG: PotionType =
            PotionType("Strong Poison", "minecraft:strong_poison", 27, 2, PotionEffects.POISON_STRONG)

        @JvmField
        val REGENERATION: PotionType =
            PotionType("Regeneration", "minecraft:regeneration", 28, PotionEffects.REGENERATION)

        @JvmField
        val REGENERATION_LONG: PotionType = PotionType(
            "Long Regeneration",
            "minecraft:long_regeneration",
            29,
            PotionEffects.REGENERATION_LONG
        )

        @JvmField
        val REGENERATION_STRONG: PotionType = PotionType(
            "Strong Regeneration",
            "minecraft:strong_regeneration",
            30,
            2,
            PotionEffects.REGENERATION_STRONG
        )

        @JvmField
        val STRENGTH: PotionType = PotionType("Strength", "minecraft:strength", 31, PotionEffects.STRENGTH)

        @JvmField
        val STRENGTH_LONG: PotionType =
            PotionType("Long Strength", "minecraft:long_strength", 32, PotionEffects.STRENGTH_LONG)

        @JvmField
        val STRENGTH_STRONG: PotionType =
            PotionType("Strong Strength", "minecraft:strong_strength", 33, 2, PotionEffects.STRENGTH_STRONG)

        @JvmField
        val WEAKNESS: PotionType = PotionType("Weakness", "minecraft:weakness", 34, PotionEffects.WEAKNESS)

        @JvmField
        val WEAKNESS_LONG: PotionType =
            PotionType("Long Weakness", "minecraft:long_weakness", 35, PotionEffects.WEAKNESS_LONG)

        @JvmField
        val WITHER: PotionType = PotionType("Wither", "minecraft:strong_wither", 36, 2, PotionEffects.WITHER)

        @JvmField
        val TURTLE_MASTER: PotionType =
            PotionType("Turtle Master", "minecraft:turtle_master", 37, PotionEffects.TURTLE_MASTER)

        @JvmField
        val TURTLE_MASTER_LONG: PotionType = PotionType(
            "Long Turtle Master",
            "minecraft:long_turtle_master",
            38,
            PotionEffects.TURTLE_MASTER_LONG
        )

        @JvmField
        val TURTLE_MASTER_STRONG: PotionType = PotionType(
            "Strong Turtle Master",
            "minecraft:strong_turtle_master",
            39,
            2,
            PotionEffects.TURTLE_MASTER_STRONG
        )

        @JvmField
        val SLOW_FALLING: PotionType =
            PotionType("Slow Falling", "minecraft:slow_falling", 40, PotionEffects.SLOW_FALLING)

        @JvmField
        val SLOW_FALLING_LONG: PotionType = PotionType(
            "Long Slow Falling",
            "minecraft:long_slow_falling",
            41,
            PotionEffects.SLOW_FALLING_LONG
        )

        @JvmField
        val SLOWNESS_STRONG: PotionType =
            PotionType("Strong Slowness", "minecraft:strong_slowness", 42, 2, PotionEffects.SLOWNESS_STRONG)

        @JvmField
        val WIND_CHARGED: PotionType =
            PotionType("Wind Charged", "minecraft:wind_charged", 43, PotionEffects.EMPTY)

        @JvmField
        val WEAVING: PotionType = PotionType("Weaving", "minecraft:weaving", 44, PotionEffects.EMPTY)

        @JvmField
        val OOZING: PotionType = PotionType("Oozing", "minecraft:oozing", 45, PotionEffects.EMPTY)

        @JvmField
        val INFESTED: PotionType = PotionType("Infested", "minecraft:infested", 46, PotionEffects.EMPTY)

        private fun appendRoman(sb: StringBuilder, num: Int) {
            var num1: Int = num
            val romans: Array<String> = arrayOf("I", "IV", "V", "IX", "X", "XL", "L", "XC", "C", "CD", "D", "CM", "M")
            val ints: IntArray = intArrayOf(1, 4, 5, 9, 10, 40, 50, 90, 100, 400, 500, 900, 1000)

            for (i in ints.indices.reversed()) {
                val times: Int = num1 / ints[i]
                num1 %= ints[i]

                sb.append(romans[i].repeat(times))
            }
        }

        @JvmStatic
        fun get(stringId: String): PotionType {
            return Registries.POTION.get(stringId) ?: throw RuntimeException("Unknown PotionType ID: $stringId")
        }

        @JvmStatic
        fun get(id: Int): PotionType {
            return Registries.POTION.get(id) ?: throw RuntimeException("Unknown PotionType ID: $id")
        }
    }
}