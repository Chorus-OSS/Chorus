package org.chorus_oss.chorus.tags

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.chorus_oss.chorus.generated.resources.Res
import org.jetbrains.annotations.UnmodifiableView
import java.io.IOException
import java.util.*

object BlockTags {
    const val ACACIA: String = "minecraft:acacia"
    const val BIRCH: String = "minecraft:birch"
    const val CROP: String = "minecraft:crop"
    const val DARK_OAK: String = "minecraft:dark_oak"
    const val DIAMOND_PICK_DIGGABLE: String = "minecraft:diamond_pick_diggable"
    const val DIAMOND_TIER_DESTRUCTIBLE: String = "minecraft:diamond_tier_destructible"
    const val DIRT: String = "minecraft:dirt"
    const val FERTILIZE_AREA: String = "minecraft:fertilize_area"
    const val GRASS: String = "minecraft:grass"
    const val GRAVEL: String = "minecraft:gravel"
    const val IRON_PICK_DIGGABLE: String = "minecraft:iron_pick_diggable"
    const val IRON_TIER_DESTRUCTIBLE: String = "minecraft:iron_tier_destructible"
    const val IS_AXE_ITEM_DESTRUCTIBLE: String = "minecraft:is_axe_item_destructible"
    const val IS_HOE_ITEM_DESTRUCTIBLE: String = "minecraft:is_hoe_item_destructible"
    const val IS_PICKAXE_ITEM_DESTRUCTIBLE: String = "minecraft:is_pickaxe_item_destructible"
    const val IS_SHEARS_ITEM_DESTRUCTIBLE: String = "minecraft:is_shears_item_destructible"
    const val IS_SHOVEL_ITEM_DESTRUCTIBLE: String = "minecraft:is_shovel_item_destructible"
    const val IS_SWORD_ITEM_DESTRUCTIBLE: String = "minecraft:is_sword_item_destructible"
    const val JUNGLE: String = "minecraft:jungle"
    const val LOG: String = "minecraft:log"
    const val METAL: String = "minecraft:metal"
    const val MOB_SPAWNER: String = "minecraft:mob_spawner"
    const val NOT_FEATURE_REPLACEABLE: String = "minecraft:not_feature_replaceable"
    const val OAK: String = "minecraft:oak"
    const val ONE_WAY_COLLIDABLE: String = "minecraft:one_way_collidable"
    const val PLANT: String = "minecraft:plant"
    const val PUMPKIN: String = "minecraft:pumpkin"
    const val RAIL: String = "minecraft:rail"
    const val SAND: String = "minecraft:sand"
    const val SNOW: String = "minecraft:snow"
    const val SPRUCE: String = "minecraft:spruce"
    const val STONE: String = "minecraft:stone"
    const val STONE_PICK_DIGGABLE: String = "minecraft:stone_pick_diggable"
    const val STONE_TIER_DESTRUCTIBLE: String = "minecraft:stone_tier_destructible"
    const val TEXT_SIGN: String = "minecraft:text_sign"
    const val TRAPDOORS: String = "minecraft:trapdoors"
    const val WATER: String = "minecraft:water"
    const val WOOD: String = "minecraft:wood"

    //PNX only
    const val PNX_WOOL: String = "pnx:wool"
    const val PNX_SHULKERBOX: String = "pnx:shulkerbox"


    private val TAG_2_BLOCKS = mutableMapOf<String, MutableSet<String>>()
    private val BLOCKS_2_TAGS = mutableMapOf<String, MutableSet<String>>()

    init {
        try {
            runBlocking { Res.readBytes("files/block_tags.json").inputStream() }.use { stream ->
                val map = Json.parseToJsonElement(stream.reader().use { it.readText() }).jsonObject.entries.associate {
                    it.key to it.value.jsonArray.map { v -> v.jsonPrimitive.content }.toMutableSet()
                }
                TAG_2_BLOCKS.putAll(map)
                for ((key, value) in TAG_2_BLOCKS) {
                    for (block in value) {
                        val tags = BLOCKS_2_TAGS.computeIfAbsent(
                            block
                        ) { HashSet() }
                        tags.add(key)
                    }
                }
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    @JvmStatic
    fun getTagSet(identifier: String?): @UnmodifiableView MutableSet<String> {
        return Collections.unmodifiableSet(BLOCKS_2_TAGS.getOrDefault(identifier, setOf()))
    }

    fun getBlockSet(tag: String?): @UnmodifiableView MutableSet<String> {
        return Collections.unmodifiableSet(TAG_2_BLOCKS.getOrDefault(tag, setOf()))
    }

    /**
     * Register Block tags for the given block identifier.
     * This is a server-side only method, DO NOT affect the client.
     *
     * @param identifier The block identifier
     * @param tags       The tags to register
     */
    fun register(identifier: String, tags: Collection<String>) {
        val tagSet = BLOCKS_2_TAGS[identifier]
        if (tagSet == null) {
            BLOCKS_2_TAGS[identifier] = HashSet(tags)
        } else tagSet.addAll(tags)

        for (tag in tags) {
            val itemSet = TAG_2_BLOCKS[tag]
            if (itemSet == null) {
                TAG_2_BLOCKS[tag] = HashSet(setOf(identifier))
            } else itemSet.add(identifier)
        }
    }
}
