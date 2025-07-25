package org.chorus_oss.chorus.entity

import org.chorus_oss.chorus.registry.Registries
import org.chorus_oss.chorus.tags.BiomeTags

interface ClimateVariant {
    fun getBiomeVariant(biomeID: Int): Variant {
        val definition = Registries.BIOME[biomeID]
        val tags = definition?.tags ?: emptySet()
        if (coldTags.any { it in tags }) return Variant.Cold
        if (warmTags.any { it in tags }) return Variant.Warm
        return Variant.Temperate
    }

    var climateVariant: Variant
        get() {
            if (this is Entity) {
                val variant = this.getEnumEntityProperty(PROPERTY_STATE)
                if (variant == null) return Variant.Temperate
                return Variant.get(variant)
            }
            return Variant.Temperate
        }
        set(value) {
            if (this is Entity) {
                this.setEnumEntityProperty(PROPERTY_STATE, value.id)
                this.sendData(this.viewers.values.toTypedArray())
                this.namedTag?.putString("variant", value.id)
            }
        }

    companion object {
        enum class Variant(val id: String) {
            Temperate("temperate"),
            Warm("warm"),
            Cold("cold");

            companion object {
                fun get(id: String): Variant {
                    return entries.find { it.id == id }!!
                }
            }
        }

        const val PROPERTY_STATE: String = "minecraft:climate_variant"

        val coldTags: List<String> = listOf(
            BiomeTags.TAIGA,
            BiomeTags.EXTREME_HILLS,
            BiomeTags.FROZEN,
            BiomeTags.DEEP_DOWN,
            BiomeTags.THE_END
        )

        val warmTags: List<String> = listOf(
            BiomeTags.SAVANNA,
            BiomeTags.JUNGLE,
            BiomeTags.MESA,
            BiomeTags.DESERT,
            BiomeTags.LUKEWARM,
            BiomeTags.SWAMP,
            BiomeTags.NETHER
        )
    }
}