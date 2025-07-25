package org.chorus_oss.chorus.utils

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ConfigTest {
    @Test
    fun test_loadConfig1() {
        val config = Config()
        val resourceAsStream = ConfigTest::class.java.classLoader.getResourceAsStream("config.yml")
        config.load(resourceAsStream)
        Assertions.assertEquals(20, config.getSection("opSlots").getInt("slotsCount"))
    }
}
