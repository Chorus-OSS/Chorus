package org.chorus_oss.chorus.utils

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class UtilsTest {
    @Test
    fun Utils_computeRequiredBits() {
        Assertions.assertEquals(2, Utils.computeRequiredBits(0, 3).toInt())
        Assertions.assertEquals(3, Utils.computeRequiredBits(0, 4).toInt())
        Assertions.assertEquals(3, Utils.computeRequiredBits(1, 8).toInt())
        Assertions.assertEquals(4, Utils.computeRequiredBits(1, 9).toInt())
    }
}
