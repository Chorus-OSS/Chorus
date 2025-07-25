package org.chorus_oss.chorus.entity.effect

import java.util.function.Function

@JvmRecord
data class PotionEffects(val supplier: Function<Boolean, List<Effect>>) {
    fun getEffects(splash: Boolean): List<Effect> {
        return supplier.apply(splash)
    }

    companion object {
        val EMPTY: PotionEffects = PotionEffects(Function { splash: Boolean? -> emptyList() })

        val NIGHT_VISION: PotionEffects = PotionEffects(Function { splash: Boolean ->
            listOf(
                EffectNightVision()
                    .setDuration(if (splash) 2700 else 3600)
            )
        })

        val NIGHT_VISION_LONG: PotionEffects = PotionEffects(Function { splash: Boolean ->
            listOf(
                EffectNightVision()
                    .setDuration(if (splash) 7200 else 9600)
            )
        })

        val INVISIBILITY: PotionEffects = PotionEffects(Function { splash: Boolean ->
            listOf(
                EffectInvisibility()
                    .setDuration(if (splash) 2700 else 3600)
            )
        })

        val INVISIBILITY_LONG: PotionEffects = PotionEffects(Function { splash: Boolean ->
            listOf(
                EffectInvisibility()
                    .setDuration(if (splash) 7200 else 9600)
            )
        })

        val LEAPING: PotionEffects = PotionEffects(Function { splash: Boolean ->
            listOf(
                EffectJumpBoost()
                    .setDuration(if (splash) 2700 else 3600)
            )
        })

        val LEAPING_LONG: PotionEffects = PotionEffects(Function { splash: Boolean ->
            listOf(
                EffectJumpBoost()
                    .setDuration(if (splash) 7200 else 9600)
            )
        })

        val LEAPING_STRONG: PotionEffects = PotionEffects(Function { splash: Boolean ->
            listOf(
                EffectJumpBoost()
                    .setDuration(if (splash) 1340 else 1800)
                    .setAmplifier(1)
            )
        })

        val FIRE_RESISTANCE: PotionEffects = PotionEffects(Function { splash: Boolean ->
            listOf(
                EffectFireResistance()
                    .setDuration(if (splash) 2700 else 3600)
            )
        })

        val FIRE_RESISTANCE_LONG: PotionEffects = PotionEffects(Function { splash: Boolean ->
            listOf(
                EffectFireResistance()
                    .setDuration(if (splash) 7200 else 9600)
            )
        })

        val SWIFTNESS: PotionEffects = PotionEffects(Function { splash: Boolean ->
            listOf(
                EffectSpeed()
                    .setDuration(if (splash) 2700 else 3600)
            )
        })

        val SWIFTNESS_LONG: PotionEffects = PotionEffects(Function { splash: Boolean ->
            listOf(
                EffectSpeed()
                    .setDuration(if (splash) 7200 else 9600)
            )
        })

        val SWIFTNESS_STRONG: PotionEffects = PotionEffects(Function { splash: Boolean ->
            listOf(
                EffectSpeed()
                    .setDuration(if (splash) 1340 else 1800)
                    .setAmplifier(1)
            )
        })

        val SLOWNESS: PotionEffects = PotionEffects(Function { splash: Boolean ->
            listOf(
                EffectSlowness()
                    .setDuration(if (splash) 1340 else 1800)
            )
        })

        val SLOWNESS_LONG: PotionEffects = PotionEffects(Function { splash: Boolean ->
            listOf(
                EffectSlowness()
                    .setDuration(if (splash) 3600 else 4800)
            )
        })

        val WATER_BREATHING: PotionEffects = PotionEffects(Function { splash: Boolean ->
            listOf(
                EffectWaterBreathing()
                    .setDuration(if (splash) 2700 else 3600)
            )
        })

        val WATER_BREATHING_LONG: PotionEffects = PotionEffects(Function { splash: Boolean ->
            listOf(
                EffectWaterBreathing()
                    .setDuration(if (splash) 7200 else 9600)
            )
        })

        val HEALING: PotionEffects =
            PotionEffects(Function { splash: Boolean? -> listOf<Effect>(EffectInstantHealth()) })

        val HEALING_STRONG: PotionEffects = PotionEffects(Function { splash: Boolean? ->
            listOf(
                EffectInstantHealth()
                    .setAmplifier(1)
            )
        })

        val HARMING: PotionEffects =
            PotionEffects(Function { splash: Boolean? -> listOf<Effect>(EffectInstantDamage()) })

        val HARMING_STRONG: PotionEffects = PotionEffects(Function { splash: Boolean? ->
            listOf(
                EffectInstantDamage()
                    .setAmplifier(1)
            )
        })

        val POISON: PotionEffects = PotionEffects(Function { splash: Boolean ->
            listOf(
                EffectPoison()
                    .setDuration(if (splash) 660 else 900)
            )
        })

        val POISON_LONG: PotionEffects = PotionEffects(Function { splash: Boolean ->
            listOf(
                EffectPoison()
                    .setDuration(if (splash) 1800 else 2400)
            )
        })

        val POISON_STRONG: PotionEffects = PotionEffects(Function { splash: Boolean ->
            listOf(
                EffectPoison()
                    .setDuration(if (splash) 320 else 440)
                    .setAmplifier(1)
            )
        })

        val REGENERATION: PotionEffects = PotionEffects(Function { splash: Boolean ->
            listOf(
                EffectRegeneration()
                    .setDuration(if (splash) 660 else 900)
            )
        })

        val REGENERATION_LONG: PotionEffects = PotionEffects(Function { splash: Boolean ->
            listOf(
                EffectRegeneration()
                    .setDuration(if (splash) 1800 else 2400)
            )
        })

        val REGENERATION_STRONG: PotionEffects = PotionEffects(Function { splash: Boolean ->
            listOf(
                EffectRegeneration()
                    .setDuration(if (splash) 320 else 440)
                    .setAmplifier(1)
            )
        })

        val STRENGTH: PotionEffects = PotionEffects(Function { splash: Boolean ->
            listOf(
                EffectStrength()
                    .setDuration(if (splash) 2700 else 3600)
            )
        })

        val STRENGTH_LONG: PotionEffects = PotionEffects(Function { splash: Boolean ->
            listOf(
                EffectStrength()
                    .setDuration(if (splash) 7200 else 9600)
            )
        })

        val STRENGTH_STRONG: PotionEffects = PotionEffects(Function { splash: Boolean ->
            listOf(
                EffectStrength()
                    .setDuration(if (splash) 1340 else 1800)
                    .setAmplifier(1)
            )
        })

        val WEAKNESS: PotionEffects = PotionEffects(Function { splash: Boolean ->
            listOf(
                EffectWeakness()
                    .setDuration(if (splash) 1340 else 1800)
            )
        })

        val WEAKNESS_LONG: PotionEffects = PotionEffects(Function { splash: Boolean ->
            listOf(
                EffectWeakness()
                    .setDuration(if (splash) 3600 else 4800)
            )
        })

        val WITHER: PotionEffects = PotionEffects(Function { splash: Boolean ->
            listOf(
                EffectWither()
                    .setDuration(if (splash) 600 else 800)
                    .setAmplifier(1)
            )
        })

        val TURTLE_MASTER: PotionEffects = PotionEffects(Function { splash: Boolean ->
            listOf(
                EffectSlowness()
                    .setDuration(if (splash) 300 else 400)
                    .setAmplifier(3),
                EffectResistance()
                    .setDuration(if (splash) 300 else 400)
                    .setAmplifier(2)
            )
        })

        val TURTLE_MASTER_LONG: PotionEffects = PotionEffects(Function { splash: Boolean ->
            listOf(
                EffectSlowness()
                    .setDuration(if (splash) 600 else 800)
                    .setAmplifier(3),
                EffectResistance()
                    .setDuration(if (splash) 600 else 800)
                    .setAmplifier(2)
            )
        })

        val TURTLE_MASTER_STRONG: PotionEffects = PotionEffects(Function { splash: Boolean ->
            listOf(
                EffectSlowness()
                    .setDuration(if (splash) 300 else 400)
                    .setAmplifier(5),
                EffectResistance()
                    .setDuration(if (splash) 300 else 400)
                    .setAmplifier(3)
            )
        })

        val SLOW_FALLING: PotionEffects = PotionEffects(Function { splash: Boolean ->
            listOf(
                EffectSlowFalling()
                    .setDuration(if (splash) 1340 else 1800)
            )
        })

        val SLOW_FALLING_LONG: PotionEffects = PotionEffects(Function { splash: Boolean ->
            listOf(
                EffectSlowFalling()
                    .setDuration(if (splash) 3600 else 4800)
            )
        })

        val SLOWNESS_STRONG: PotionEffects = PotionEffects(Function { splash: Boolean ->
            listOf(
                EffectSlowness()
                    .setDuration(if (splash) 300 else 400)
                    .setAmplifier(3)
            )
        })
    }
}
