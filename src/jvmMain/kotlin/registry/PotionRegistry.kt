package org.chorus_oss.chorus.registry

import org.chorus_oss.chorus.entity.effect.PotionType
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class PotionRegistry : IRegistry<String, PotionType?, PotionType> {
    override fun init() {
        if (isLoad.getAndSet(true)) return
        register0(PotionType.WATER)
        register0(PotionType.MUNDANE)
        register0(PotionType.MUNDANE_LONG)
        register0(PotionType.THICK)
        register0(PotionType.AWKWARD)
        register0(PotionType.NIGHT_VISION)
        register0(PotionType.NIGHT_VISION_LONG)
        register0(PotionType.INVISIBILITY)
        register0(PotionType.INVISIBILITY_LONG)
        register0(PotionType.LEAPING)
        register0(PotionType.LEAPING_LONG)
        register0(PotionType.LEAPING_STRONG)
        register0(PotionType.FIRE_RESISTANCE)
        register0(PotionType.FIRE_RESISTANCE_LONG)
        register0(PotionType.SWIFTNESS)
        register0(PotionType.SWIFTNESS_LONG)
        register0(PotionType.SWIFTNESS_STRONG)
        register0(PotionType.SLOWNESS)
        register0(PotionType.SLOWNESS_LONG)
        register0(PotionType.WATER_BREATHING)
        register0(PotionType.WATER_BREATHING_LONG)
        register0(PotionType.HEALING)
        register0(PotionType.HEALING_STRONG)
        register0(PotionType.HARMING)
        register0(PotionType.HARMING_STRONG)
        register0(PotionType.POISON)
        register0(PotionType.POISON_LONG)
        register0(PotionType.POISON_STRONG)
        register0(PotionType.REGENERATION)
        register0(PotionType.REGENERATION_LONG)
        register0(PotionType.REGENERATION_STRONG)
        register0(PotionType.STRENGTH)
        register0(PotionType.STRENGTH_LONG)
        register0(PotionType.STRENGTH_STRONG)
        register0(PotionType.WEAKNESS)
        register0(PotionType.WEAKNESS_LONG)
        register0(PotionType.WITHER)
        register0(PotionType.TURTLE_MASTER)
        register0(PotionType.TURTLE_MASTER_LONG)
        register0(PotionType.TURTLE_MASTER_STRONG)
        register0(PotionType.SLOW_FALLING)
        register0(PotionType.SLOW_FALLING_LONG)
        register0(PotionType.SLOWNESS_STRONG)
        register0(PotionType.WIND_CHARGED)
        register0(PotionType.WEAVING)
        register0(PotionType.OOZING)
        register0(PotionType.INFESTED)
    }

    override fun get(key: String): PotionType? {
        return REGISTRY[key]
    }

    fun get(id: Int): PotionType? {
        return ID_2_POTION[id]
    }

    val potions: Map<String, PotionType?>
        get() = Collections.unmodifiableMap(REGISTRY)

    val potionId2TypeMap: Map<Int, PotionType>
        get() = Collections.unmodifiableMap(ID_2_POTION)

    override fun reload() {
        isLoad.set(false)
        REGISTRY.clear()
        ID_2_POTION.clear()
        init()
    }

    @Throws(RegisterException::class)
    override fun register(key: String, value: PotionType) {
        if (REGISTRY.putIfAbsent(key, value) == null) {
            ID_2_POTION[value.id] = value
        } else {
            throw RegisterException("This potion has already been registered with the identifier: $key")
        }
    }

    private fun register0(value: PotionType) {
        try {
            register(value.stringId, value)
        } catch (e: RegisterException) {
            throw RuntimeException(e)
        }
    }

    companion object {
        private val REGISTRY = mutableMapOf<String, PotionType?>()
        private val ID_2_POTION = mutableMapOf<Int, PotionType>()
        private val isLoad = AtomicBoolean(false)
    }
}
