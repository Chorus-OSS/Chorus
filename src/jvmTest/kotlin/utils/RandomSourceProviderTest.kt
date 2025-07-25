package org.chorus_oss.chorus.utils

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class RandomSourceProviderTest {
    @Test
    fun nextInt() {
        val i = randomSource!!.nextInt()
        Assertions.assertTrue(Int.MAX_VALUE >= i && i >= Int.MIN_VALUE)
    }

    @Test
    fun nextIntBound() {
        val i = randomSource!!.nextInt(5)
        Assertions.assertTrue(5 >= i && i >= 0)
    }

    @Test
    fun nextIntMinMax() {
        val i = randomSource!!.nextInt(4, 10)
        Assertions.assertTrue(10 >= i && i >= 4)
    }

    @Test
    fun nextGaussian() {
        for (i in 0..99) {
            val v = randomSource!!.nextGaussian()
            Assertions.assertTrue(v <= 1 && v >= -1, v.toString())
        }
    }

    companion object {
        var randomSource: ChorusRandom? = null

        @BeforeAll
        fun test_create() {
            randomSource = ChorusRandom.create()
        }
    }
}
