package org.chorus_oss.chorus.tags

import org.chorus_oss.chorus.registry.BiomeRegistry.BiomeDefinition
import org.chorus_oss.chorus.registry.Registries

object BiomeTags {
    const val ANIMAL: String = "animal"
    const val BAMBOO: String = "bamboo"
    const val BASALT_DELTAS: String = "basalt_deltas"
    const val BEACH: String = "beach"
    const val BEE_HABITAT: String = "bee_habitat"
    const val BIRCH: String = "birch"
    const val CAVES: String = "caves"
    const val COLD: String = "cold"
    const val CRIMSON_FOREST: String = "crimson_forest"
    const val DEEP_DOWN: String = "deep_down"
    const val DESERT: String = "desert"
    const val DRIPSTONE_CAVES: String = "dripstone_caves"
    const val EDGE: String = "edge"
    const val EXTREME_HILLS: String = "extreme_hills"
    const val FLOWER_FOREST: String = "flower_forest"
    const val FOREST: String = "forest"
    const val FROZEN: String = "frozen"
    const val FROZEN_PEAKS: String = "frozen_peaks"
    const val GROVE: String = "grove"
    const val HILLS: String = "hills"
    const val ICE: String = "ice"
    const val ICE_PLAINS: String = "ice_plains"
    const val JAGGED_PEAKS: String = "jagged_peaks"
    const val JUNGLE: String = "jungle"
    const val LUKEWARM: String = "lukewarm"
    const val LUSH_CAVES: String = "lush_caves"
    const val MEADOW: String = "meadow"
    const val MEGA: String = "mega"
    const val MESA: String = "mesa"
    const val MONSTER: String = "monster"
    const val MOOSHROOM_ISLAND: String = "mooshroom_island"
    const val MOUNTAIN: String = "mountain"
    const val MOUNTAINS: String = "mountains"
    const val MUTATED: String = "mutated"
    const val NETHER: String = "nether"
    const val NETHER_WASTES: String = "nether_wastes"
    const val NETHERWART_FOREST: String = "netherwart_forest"
    const val NO_LEGACY_WORLDOGEN: String = "no_legacy_worldgen"
    const val OCEAN: String = "ocean"
    const val OVERWORLD: String = "overworld"
    const val OVERWORLD_GENERATION: String = "overworld_generation"
    const val PLAINS: String = "plains"
    const val PLATEAU: String = "plateau"
    const val RARE: String = "rare"
    const val RIVER: String = "river"
    const val ROOFED: String = "roofed"
    const val SAVANNA: String = "savanna"
    const val SHORE: String = "shore"
    const val SNOWY_SLOPES: String = "snowy_slopes"
    const val SOULSAND_VALLEY: String = "soulsand_valley"
    const val SPAWN_ENDERMEN: String = "spawn_endermen"
    const val SPAWN_FEW_PIGLINS: String = "spawn_few_piglins"
    const val SPAWN_FEW_ZOMBIFIED_PIGLINS: String = "spawn_few_zombified_piglins"
    const val SPAWN_GHAST: String = "spawn_ghast"
    const val SPAWN_MAGMA_CUBES: String = "spawn_magma_cubes"
    const val SPAWN_MANY_MAGMA_CUBES: String = "spawn_many_magma_cubes"
    const val SPAWN_PIGLIN: String = "spawn_piglin"
    const val SPAWN_ZOMBIFIED_PIGLIN: String = "spawn_zombified_piglin"
    const val STONE: String = "stone"
    const val SWAMP: String = "swamp"
    const val TAIGA: String = "taiga"
    const val THE_END: String = "the_end"
    const val WARM: String = "warm"
    const val WARPED_FOREST: String = "warped_forest"


    private val TAG_2_BIOMES = HashMap<String, MutableSet<String>>()

    init {
        val biomeDefinitions: Set<BiomeDefinition> = Registries.BIOME.biomeDefinitions
        val tmpMap = HashMap<String, Set<String>>()
        for (def in biomeDefinitions) {
            tmpMap[def.name] = HashSet(def.tags)
        }
        for ((key, value) in tmpMap) {
            for (biomeTag in value) {
                val tags = TAG_2_BIOMES.computeIfAbsent(
                    biomeTag
                ) { HashSet() }
                tags.add(key)
            }
        }
    }

    fun containTag(biomeId: Int, tag: String?): Boolean {
        return Registries.BIOME[biomeId]!!.tags.contains(tag)
    }

    fun containTag(biomeName: String, tag: String): Boolean {
        return Registries.BIOME.get(biomeName)!!.tags.contains(tag)
    }

    fun getTagSet(biomeName: String): Set<String> {
        return Registries.BIOME.get(biomeName)!!.tags
    }

    fun getBiomeSet(tag: String): Set<String> {
        return TAG_2_BIOMES.getOrDefault(tag, setOf())
    }

    fun register(definition: BiomeDefinition) {
        val name = definition.name
        val tags = definition.tags
        for (tag in tags) {
            val itemSet = TAG_2_BIOMES[tag]
            itemSet?.add(name)
                ?: TAG_2_BIOMES.set(tag, HashSet(setOf(name)))
        }
    }
}
