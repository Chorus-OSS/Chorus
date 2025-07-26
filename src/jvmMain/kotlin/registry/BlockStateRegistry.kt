package org.chorus_oss.chorus.registry

import com.google.gson.JsonParser
import kotlinx.coroutines.runBlocking
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockState
import org.chorus_oss.chorus.generated.resources.Res
import org.chorus_oss.chorus.utils.BlockColor
import org.chorus_oss.chorus.utils.Loggable
import org.jetbrains.annotations.ApiStatus
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class BlockStateRegistry : IRegistry<Int, BlockState?, BlockState> {
    override fun init() {
        try {
            runBlocking { Res.readBytes("files/gamedata/endstone/block_states.json").inputStream() }.use { stream ->
                val reader = BufferedReader(InputStreamReader(stream))
                val blockStateData = JsonParser.parseReader(reader).asJsonArray
                for (i in 0..<blockStateData.size()) {
                    val entry = blockStateData[i].asJsonObject
                    val hash = entry["blockStateHash"].asInt
                    val name = entry["name"].asString
                    if (BlockRegistry.skipBlockSet.contains(name)) continue
                    val state = Registries.BLOCKSTATE[hash]
                    if (state == null) {
                        log.warn("$name ($hash) was not a part of block_states.json.")
                    } else {
                        if (state.identifier != name) {
                            log.warn("BlockState " + hash + " was not " + name + ". Instead it is " + state.identifier)
                        }
                    }
                    val hexString = entry["mapColor"].asString.substring(1, 9)
                    val r = hexString.substring(0, 2).toInt(16)
                    val g = hexString.substring(2, 4).toInt(16)
                    val b = hexString.substring(4, 6).toInt(16)
                    val a = hexString.substring(6, 8).toInt(16)
                    Block.VANILLA_BLOCK_COLOR_MAP[hash.toLong()] = BlockColor(r, g, b, a)
                }
            }
        } catch (_: IOException) {
        }
    }

    override operator fun get(key: Int): BlockState? {
        return REGISTRY[key]
    }

    val allState: Set<BlockState>
        get() = REGISTRY.values.toSet()

    override fun reload() {
        REGISTRY.clear()
    }

    @Throws(RegisterException::class)
    override fun register(key: Int, value: BlockState) {
        if (REGISTRY.putIfAbsent(key, value) != null) {
            throw RegisterException("The blockstate has been registered!")
        }
    }

    @Throws(RegisterException::class)
    fun register(value: BlockState) {
        val now: BlockState?
        if ((REGISTRY.put(value.blockStateHash(), value).also { now = it }) != null) {
            throw RegisterException("The blockstate ${value} has been registered, current value: $now")
        }
    }

    @ApiStatus.Internal
    fun registerInternal(value: BlockState) {
        try {
            register(value)
        } catch (e: RegisterException) {
            throw RuntimeException(e)
        }
    }

    companion object : Loggable {
        private val REGISTRY = HashMap<Int, BlockState>()
    }
}
