package org.chorus_oss.chorus.entity.effect

import org.chorus_oss.chorus.registry.Registries

data class EffectType(@JvmField val stringId: String, @JvmField val id: Int) {
    override fun equals(other: Any?): Boolean {
        if (other is EffectType) {
            return other.stringId == this.stringId && other.id == this.id
        }
        return false
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    companion object {
        @JvmField
        val SPEED: EffectType = EffectType("speed", 1)

        @JvmField
        val SLOWNESS: EffectType = EffectType("slowness", 2)

        @JvmField
        val HASTE: EffectType = EffectType("haste", 3)

        @JvmField
        val MINING_FATIGUE: EffectType = EffectType("mining_fatigue", 4)

        @JvmField
        val STRENGTH: EffectType = EffectType("strength", 5)

        @JvmField
        val INSTANT_HEALTH: EffectType = EffectType("instant_health", 6)

        @JvmField
        val INSTANT_DAMAGE: EffectType = EffectType("instant_damage", 7)

        @JvmField
        val JUMP_BOOST: EffectType = EffectType("jump_boost", 8)

        @JvmField
        val NAUSEA: EffectType = EffectType("nausea", 9)

        @JvmField
        val REGENERATION: EffectType = EffectType("regeneration", 10)

        @JvmField
        val RESISTANCE: EffectType = EffectType("resistance", 11)

        @JvmField
        val FIRE_RESISTANCE: EffectType = EffectType("fire_resistance", 12)

        @JvmField
        val WATER_BREATHING: EffectType = EffectType("water_breathing", 13)

        @JvmField
        val INVISIBILITY: EffectType = EffectType("invisibility", 14)

        @JvmField
        val BLINDNESS: EffectType = EffectType("blindness", 15)

        @JvmField
        val NIGHT_VISION: EffectType = EffectType("night_vision", 16)

        @JvmField
        val HUNGER: EffectType = EffectType("hunger", 17)

        @JvmField
        val WEAKNESS: EffectType = EffectType("weakness", 18)

        @JvmField
        val POISON: EffectType = EffectType("poison", 19)

        @JvmField
        val WITHER: EffectType = EffectType("wither", 20)

        @JvmField
        val HEALTH_BOOST: EffectType = EffectType("health_boost", 21)

        @JvmField
        val ABSORPTION: EffectType = EffectType("absorption", 22)

        @JvmField
        val SATURATION: EffectType = EffectType("saturation", 23)

        @JvmField
        val LEVITATION: EffectType = EffectType("levitation", 24)

        @JvmField
        val FATAL_POISON: EffectType = EffectType("fatal_poison", 25)

        @JvmField
        val CONDUIT_POWER: EffectType = EffectType("conduit_power", 26)

        @JvmField
        val SLOW_FALLING: EffectType = EffectType("slow_falling", 27)

        val BAD_OMEN: EffectType = EffectType("bad_omen", 28)

        val VILLAGE_HERO: EffectType = EffectType("village_hero", 29)

        @JvmField
        val DARKNESS: EffectType = EffectType("darkness", 30)

        fun get(stringId: String): EffectType {
            return Registries.EFFECT.getType(stringId) ?: throw RuntimeException("Unknown stringId: $stringId")
        }

        fun get(id: Int): EffectType {
            return Registries.EFFECT.getType(id) ?: throw RuntimeException("Unknown id: $id")
        }
    }
}
