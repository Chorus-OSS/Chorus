package org.chorus_oss.chorus.utils

import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.TreeMapCompoundTag
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class HashUtilsTest {
    @Test
    fun hashBlockState() {
        val compoundTag = CompoundTag()

        val state = TreeMapCompoundTag()
        state.putBoolean("button_pressed_bit", false)
            .putInt("facing_direction", 5)

        compoundTag.putString("name", "minecraft:warped_button")
            .putCompound("states", state)
        val i = HashUtils.fnv1a_32_nbt(compoundTag)
        Assertions.assertEquals(1204504330, i)
    }
}
