package org.chorus_oss.chorus.block

import com.google.gson.JsonParser
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.registry.BlockRegistry
import org.chorus_oss.chorus.registry.Registries
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader

class BlockStateTest internal constructor() {
    init {
        Registries.BLOCK.init()
        var blocks = 0
        FileInputStream("src/main/resources/gamedata/endstone/block_states.json").use { stream ->
            val reader = BufferedReader(InputStreamReader(stream))
            val blockStateData = JsonParser.parseReader(reader).asJsonArray
            for (i in 0..<blockStateData.size()) {
                val entry = blockStateData[i].asJsonObject
                val hash = entry["blockStateHash"].asInt
                val name = entry["name"].asString
                if (BlockRegistry.skipBlockSet.contains(name)) continue
                val state = Registries.BLOCKSTATE[hash]
                if (state == null) {
                    throw RuntimeException("$name ($hash) was not a part of block_states.json.")
                } else {
                    if (state.identifier != name) {
                        throw RuntimeException("BlockState " + hash + " was not " + name + ". Instead it is " + state.identifier)
                    }
                }
                blocks++
            }
            Assertions.assertEquals(blocks, Registries.BLOCKSTATE.allState.size)
        }
    }

    @Test
    fun BlockStateImpl_computeSpecialValue() {
        val i1 = BlockState.computeSpecialValue(
            mutableListOf(
                CommonBlockProperties.DIRECTION.createValue(1),  //2bit
                CommonBlockProperties.OPEN_BIT.createValue(false),  //1bit
                CommonBlockProperties.UPSIDE_DOWN_BIT.createValue(false) //1bit
            )
        )
        Assertions.assertEquals((1 shl 2 or (0 shl 1) or 0), i1.toInt())
    }
}
