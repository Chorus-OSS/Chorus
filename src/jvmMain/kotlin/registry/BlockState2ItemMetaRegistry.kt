package org.chorus_oss.chorus.registry

import org.chorus_oss.chorus.nbt.NBTIO.readCompressed
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.IntTag
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

class BlockState2ItemMetaRegistry : IRegistry<String, Int?, Int> {
    override fun init() {
        if (isLoad.getAndSet(true)) return
        try {
            BlockState2ItemMetaRegistry::class.java.classLoader.getResourceAsStream("item_meta_block_state_bimap.nbt")
                .use { input ->
                    if (input == null) {
                        throw RuntimeException("Failed to load item_meta_block_state_bimap.nbt")
                    }
                    val compoundTag = readCompressed(input)
                    for ((key, value) in compoundTag.tags) {
                        for ((key1, value1) in (value as CompoundTag).tags) {
                            MAP["$key#$key1"] = (value1 as IntTag).data
                        }
                    }
                }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    override fun reload() {
        isLoad.set(false)
        MAP.clear()
        init()
    }

    override fun get(key: String): Int {
        return MAP[key] ?: throw RuntimeException("Unknown BlockState2ItemMeta key: $key")
    }

    operator fun get(key: String, meta: Int): Int? {
        return MAP["$key#$meta"]
    }

    @Throws(RegisterException::class)
    override fun register(key: String, value: Int) {
        if (MAP.putIfAbsent(key, value) != 0) {
            throw RegisterException("The mapping has been registered!")
        }
    }

    companion object {
        private val MAP = HashMap<String, Int>()
        private val isLoad = AtomicBoolean(false)
    }
}
