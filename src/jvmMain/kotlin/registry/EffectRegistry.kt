package org.chorus_oss.chorus.registry

import org.chorus_oss.chorus.entity.effect.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.primaryConstructor

class EffectRegistry :
    IRegistry<EffectType, Effect?, KClass<out Effect>> {
    override fun init() {
        register(EffectType.SPEED, EffectSpeed::class)
        register(EffectType.SLOWNESS, EffectSlowness::class)
        register(EffectType.HASTE, EffectHaste::class)
        register(EffectType.MINING_FATIGUE, EffectMiningFatigue::class)
        register(EffectType.STRENGTH, EffectStrength::class)
        register(EffectType.INSTANT_HEALTH, EffectInstantHealth::class)
        register(EffectType.INSTANT_DAMAGE, EffectInstantDamage::class)
        register(EffectType.JUMP_BOOST, EffectJumpBoost::class)
        register(EffectType.NAUSEA, EffectNausea::class)
        register(EffectType.REGENERATION, EffectRegeneration::class)
        register(EffectType.RESISTANCE, EffectResistance::class)
        register(EffectType.FIRE_RESISTANCE, EffectFireResistance::class)
        register(EffectType.WATER_BREATHING, EffectWaterBreathing::class)
        register(EffectType.INVISIBILITY, EffectInvisibility::class)
        register(EffectType.BLINDNESS, EffectBlindness::class)
        register(EffectType.NIGHT_VISION, EffectNightVision::class)
        register(EffectType.HUNGER, EffectHunger::class)
        register(EffectType.WEAKNESS, EffectWeakness::class)
        register(EffectType.POISON, EffectPoison::class)
        register(EffectType.WITHER, EffectWither::class)
        register(EffectType.HEALTH_BOOST, EffectHealthBoost::class)
        register(EffectType.ABSORPTION, EffectAbsorption::class)
        register(EffectType.SATURATION, EffectSaturation::class)
        register(EffectType.LEVITATION, EffectLevitation::class)
        register(EffectType.FATAL_POISON, EffectFatalPoison::class)
        register(EffectType.SLOW_FALLING, EffectSlowFalling::class)
        register(EffectType.CONDUIT_POWER, EffectConduitPower::class)
        // Effects that cannot be realized at the moment
        //register(EffectType.BAD_OMEN, BadOmenEffect.class);
        //register(EffectType.VILLAGE_HERO, VillageHeroEffect.class);
        register(EffectType.DARKNESS, EffectDarkness::class)
    }

    override fun get(key: EffectType): Effect? {
        try {
            val constructor = CACHE_CONSTRUCTORS[key] ?: return null
            return constructor.call()
        } catch (e: Throwable) {
            throw RuntimeException(e)
        }
    }

    fun getType(stringId: String): EffectType? {
        return STRING_ID_2_TYPE[stringId]
    }

    fun getType(id: Int): EffectType? {
        return INT_ID_2_TYPE[id]
    }

    val effectStringId2TypeMap: Map<String, EffectType>
        get() = STRING_ID_2_TYPE

    val effectId2TypeMap: Map<Int, EffectType>
        get() = INT_ID_2_TYPE

    override fun reload() {
        INT_ID_2_TYPE.clear()
        STRING_ID_2_TYPE.clear()
        CACHE_CONSTRUCTORS.clear()
        init()
    }

    @Throws(RegisterException::class)
    override fun register(key: EffectType, value: KClass<out Effect>) {
        try {
            val c = value.primaryConstructor ?: throw RegisterException("Effect must have primary constructor")
            if (CACHE_CONSTRUCTORS.putIfAbsent(key, c) == null) {
                STRING_ID_2_TYPE[key.stringId] = key
                INT_ID_2_TYPE[key.id] = key
            } else {
                throw RegisterException("This effect has already been registered with the identifier: $key")
            }
        } catch (e: NoSuchMethodException) {
            throw RegisterException(e)
        } catch (e: Throwable) {
            throw RuntimeException(e)
        }
    }

    companion object {
        private val CACHE_CONSTRUCTORS = HashMap<EffectType, KFunction<Effect>>()
        private val STRING_ID_2_TYPE = HashMap<String, EffectType>()
        private val INT_ID_2_TYPE = HashMap<Int, EffectType>()
    }
}
