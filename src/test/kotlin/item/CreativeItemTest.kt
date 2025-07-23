package org.chorus_oss.chorus.item

import com.google.gson.Gson
import org.chorus_oss.chorus.nbt.NBTIO
import org.chorus_oss.chorus.registry.CreativeItemRegistry
import org.chorus_oss.chorus.registry.Registries
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteOrder
import java.util.*

class CreativeItemTest {
    @Test
    fun init() {
        Registries.BLOCK.init()
        Registries.POTION.init()
        Registries.ITEM.init()
        Assertions.assertDoesNotThrow {
            try {
                CreativeItemRegistry::class.java.classLoader.getResourceAsStream("creative_items.json").use { input ->
                    val data =
                        Gson().fromJson<Map<*, *>>(
                            InputStreamReader(input),
                            MutableMap::class.java
                        )
                    val items =
                        data["items"] as List<Map<String, Any>>?
                    for (i in items!!.indices) {
                        val tag = items[i]
                        val damage = (tag.getOrDefault("damage", 0) as Number).toInt()
                        val nbt = if (tag.containsKey("nbt_b64")) Base64.getDecoder()
                            .decode(tag["nbt_b64"].toString()) else byteArrayOf()
                        val name = tag["id"].toString()
                        val item = Item.get(name, damage, 1, nbt, false)
                        require(
                            !(item.isNothing || (item.isBlock() && item.getSafeBlockState().toBlock().isAir))
                        ) { "creative index $i $name" }
                        val isBlock = tag.containsKey("block_state_b64")
                        if (isBlock) {
                            val blockTag =
                                Base64.getDecoder().decode(tag["block_state_b64"].toString())
                            val blockCompoundTag = NBTIO.read(blockTag, ByteOrder.LITTLE_ENDIAN)
                            val blockHash = blockCompoundTag.getInt("network_id")
                            val block =
                                Registries.BLOCKSTATE[blockHash]
                            requireNotNull(block) { "creative index $i $blockHash" }
                            item.blockState = block
                        }
                    }
                }
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
    }
}
