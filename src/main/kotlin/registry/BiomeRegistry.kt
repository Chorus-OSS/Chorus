package org.chorus_oss.chorus.registry

import com.google.gson.GsonBuilder
import org.chorus_oss.chorus.experimental.network.protocol.utils.biome.invoke
import org.chorus_oss.chorus.nbt.NBTIO
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.nbt.tag.StringTag
import org.chorus_oss.chorus.registry.BiomeRegistry.BiomeDefinition
import org.chorus_oss.protocol.packets.BiomeDefinitionListPacket
import org.chorus_oss.protocol.types.biome.BiomeDefinitionData
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.atomic.AtomicBoolean

class BiomeRegistry : IRegistry<Int, BiomeDefinition?, BiomeDefinition> {
    override fun init() {
        if (isLoad.getAndSet(true)) return
        try {
            BiomeRegistry::class.java.classLoader.getResourceAsStream("gamedata/kaooot/biomes.json").use { stream ->
                requireNotNull(stream) { "Couldn't load \"gamedata/kaooot/biomes.json\"" }

                val gson = GsonBuilder().setObjectToNumberStrategy { it.nextInt() }.create()
                val map: Map<String, *> = gson.fromJson<Map<String, *>>(
                    InputStreamReader(stream),
                    MutableMap::class.java
                )
                for ((key, value) in map) {
                    NAME2ID[key] = (value as Map<*, *>)["id"].toString().toInt()
                }
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        try {
            BiomeRegistry::class.java.classLoader.getResourceAsStream("gamedata/kaooot/biome_definitions.nbt")
                .use { stream ->
                    requireNotNull(stream) { "Couldn't load \"gamedata/kaooot/biome_definitions.nbt\"" }

                    val root = NBTIO.readCompressed(stream)
                    BIOME_STRINGS.addAll(root.getList("biomeStringList", StringTag::class.java).all.map { it.data })

                    val biomeData: ListTag<CompoundTag> = root.getList("biomeData", CompoundTag::class.java)
                    for (biomeTag in biomeData.all) {
                        val index = biomeTag.getShort("index")
                        val biomeID = NAME2ID[BIOME_STRINGS[index.toInt()]]!!
                        val definition = BiomeDefinition(
                            index,
                            data = BiomeDefinitionData(biomeTag.getCompound("data"))
                        )
                        register(biomeID, definition)
                    }
                }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    override operator fun get(key: Int): BiomeDefinition? {
        return DEFINITIONS[key]
    }

    fun get(biomeName: String): BiomeDefinition? {
        return get(NAME2ID[biomeName] ?: return null)
    }

    fun getBiomeId(biomeName: String): Int? {
        return NAME2ID[biomeName]
    }

    val biomeDefinitionListPacket: BiomeDefinitionListPacket
        get() = BiomeDefinitionListPacket(
            biomeDefinitions = DEFINITIONS.values.toList().associate { it.index to it.data }.toMap(),
            biomeStringList = BIOME_STRINGS
        )

    val biomeDefinitions: Set<BiomeDefinition>
        get() = DEFINITIONS.values.toSet()

    override fun reload() {
        isLoad.set(false)
        DEFINITIONS.clear()
        NAME2ID.clear()
        init()
    }

    @Throws(RegisterException::class)
    override fun register(key: Int, value: BiomeDefinition) {
        if (DEFINITIONS.putIfAbsent(key, value) == null) {
            NAME2ID[BIOME_STRINGS[value.index.toInt()]] = key
        } else {
            throw RegisterException("This biome has already been registered with the id: $key")
        }
    }

    @JvmRecord
    data class BiomeDefinition(
        val index: Short,
        val data: BiomeDefinitionData,
    ) {
        val tags: Set<String>
            get() = data.tags?.tags?.map { BIOME_STRINGS[it.toInt()] }?.toSet() ?: emptySet()

        val name: String
            get() = BIOME_STRINGS[index.toInt()]
    }

    companion object {
        private val DEFINITIONS = HashMap<Int, BiomeDefinition>(0xFF)
        private val NAME2ID = HashMap<String, Int>(0xFF)
        private val BIOME_STRINGS = mutableListOf<String>()
        private val isLoad = AtomicBoolean(false)
    }
}
