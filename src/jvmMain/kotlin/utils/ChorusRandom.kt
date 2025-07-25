package org.chorus_oss.chorus.utils

import org.apache.commons.rng.RestorableUniformRandomProvider
import org.apache.commons.rng.sampling.distribution.ContinuousSampler
import org.apache.commons.rng.sampling.distribution.GaussianSampler
import org.apache.commons.rng.sampling.distribution.ZigguratSampler
import org.apache.commons.rng.simple.RandomSource
import kotlin.math.max
import kotlin.math.min

class ChorusRandom {
    val provider: RestorableUniformRandomProvider
    val sampler: ContinuousSampler

    constructor() {
        provider = RandomSource.MT.create()
        sampler = GaussianSampler.of(
            ZigguratSampler.NormalizedGaussian.of(RandomSource.ISAAC.create()),
            0.0, 0.33333
        )
    }

    constructor(seeds: Long) {
        provider = RandomSource.MT.create(seeds)
        sampler = GaussianSampler.of(
            ZigguratSampler.NormalizedGaussian.of(RandomSource.ISAAC.create()),
            0.0, 0.33333
        )
    }

    fun nextInt(min: Int, max: Int): Int {
        return provider.nextInt(min, max + 1)
    }

    fun nextRange(min: Int, max: Int): Int {
        return provider.nextInt(min, max + 1)
    }

    fun nextBoundedInt(bound: Int): Int {
        return nextInt(bound)
    }

    fun nextInt(max: Int): Int {
        return provider.nextInt(max + 1)
    }

    fun nextInt(): Int {
        return provider.nextInt()
    }

    fun nextLong(): Long {
        return provider.nextLong()
    }

    fun nextDouble(): Double {
        return provider.nextDouble()
    }

    fun nextGaussian(): Double {
        val sample = sampler.sample()
        return min(1.0, max(sample, -1.0))
    }

    fun nextFloat(): Float {
        return provider.nextFloat()
    }

    fun nextBoolean(): Boolean {
        return provider.nextBoolean()
    }

    companion object {
        @JvmStatic
        fun create(): ChorusRandom {
            return ChorusRandom()
        }

        fun create(seed: Long): ChorusRandom {
            return ChorusRandom(seed)
        }
    }
}
