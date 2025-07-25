package org.chorus_oss.chorus.tags

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.chorus_oss.chorus.Server
import org.jetbrains.annotations.UnmodifiableView
import java.io.IOException
import java.util.*

object ItemTags {
    const val ARROW: String = "minecraft:arrow"
    const val BANNER: String = "minecraft:banner"
    const val BOAT: String = "minecraft:boat"
    const val BOATS: String = "minecraft:boats"
    const val CHAINMAIL_TIER: String = "minecraft:chainmail_tier"
    const val COALS: String = "minecraft:coals"
    const val CRIMSON_STEMS: String = "minecraft:crimson_stems"
    const val DIAMOND_TIER: String = "minecraft:diamond_tier"
    const val DIGGER: String = "minecraft:digger"
    const val DOOR: String = "minecraft:door"
    const val GOLDEN_TIER: String = "minecraft:golden_tier"
    const val HANGING_ACTOR: String = "minecraft:hanging_actor"
    const val HANGING_SIGN: String = "minecraft:hanging_sign"
    const val HORSE_ARMOR: String = "minecraft:horse_armor"
    const val IRON_TIER: String = "minecraft:iron_tier"
    const val IS_ARMOR: String = "minecraft:is_armor"
    const val IS_AXE: String = "minecraft:is_axe"
    const val IS_COOKED: String = "minecraft:is_cooked"
    const val IS_FISH: String = "minecraft:is_fish"
    const val IS_FOOD: String = "minecraft:is_food"
    const val IS_HOE: String = "minecraft:is_hoe"
    const val IS_MEAT: String = "minecraft:is_meat"
    const val IS_MINECART: String = "minecraft:is_minecart"
    const val IS_PICKAXE: String = "minecraft:is_pickaxe"
    const val IS_SHOVEL: String = "minecraft:is_shovel"
    const val IS_SWORD: String = "minecraft:is_sword"
    const val IS_TOOL: String = "minecraft:is_tool"
    const val LEATHER_TIER: String = "minecraft:leather_tier"
    const val LECTERN_BOOKS: String = "minecraft:lectern_books"
    const val LOGS: String = "minecraft:logs"
    const val LOGS_THAT_BURN: String = "minecraft:logs_that_burn"
    const val MANGROVE_LOGS: String = "minecraft:mangrove_logs"
    const val MUSIC_DISC: String = "minecraft:music_disc"
    const val NETHERITE_TIER: String = "minecraft:netherite_tier"
    const val PLANKS: String = "minecraft:planks"
    const val SAND: String = "minecraft:sand"
    const val SIGN: String = "minecraft:sign"
    const val SOUL_FIRE_BASE_BLOCKS: String = "minecraft:soul_fire_base_blocks"
    const val SPAWN_EGG: String = "minecraft:spawn_egg"
    const val STONE_BRICKS: String = "minecraft:stone_bricks"
    const val STONE_CRAFTING_MATERIALS: String = "minecraft:stone_crafting_materials"
    const val STONE_TIER: String = "minecraft:stone_tier"
    const val STONE_TOOL_MATERIALS: String = "minecraft:stone_tool_materials"
    const val VIBRATION_DAMPER: String = "minecraft:vibration_damper"
    const val WARPED_STEMS: String = "minecraft:warped_stems"
    const val WOODEN_SLABS: String = "minecraft:wooden_slabs"
    const val WOODEN_TIER: String = "minecraft:wooden_tier"
    const val WOOL: String = "minecraft:wool"

    private val TAG_2_ITEMS = mutableMapOf<String, MutableSet<String>>()
    private val ITEM_2_TAGS = mutableMapOf<String, MutableSet<String>>()

    init {
        try {
            Server::class.java.classLoader.getResourceAsStream("item_tags.json").use { stream ->
                checkNotNull(stream)

                val map = Json.parseToJsonElement(stream.reader().use { it.readText() }).jsonObject.entries.associate {
                    it.key to it.value.jsonArray.map { v -> v.jsonPrimitive.content }.toMutableSet()
                }
                TAG_2_ITEMS.putAll(map)
                for ((key, value) in TAG_2_ITEMS) {
                    for (item in value) {
                        val tags = ITEM_2_TAGS.computeIfAbsent(
                            item
                        ) { HashSet() }
                        tags.add(key)
                    }
                }
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun getTagSet(identifier: String?): @UnmodifiableView MutableSet<String> {
        return Collections.unmodifiableSet(ITEM_2_TAGS.getOrDefault(identifier, setOf()))
    }

    @JvmStatic
    fun getItemSet(tag: String?): @UnmodifiableView MutableSet<String> {
        return Collections.unmodifiableSet(TAG_2_ITEMS.getOrDefault(tag, setOf()))
    }

    /**
     * Register item tags for the given item identifier.
     * This is a server-side only method, DO NOT affect the client.
     *
     * @param identifier The item identifier
     * @param tags       The tags to register
     */
    fun register(identifier: String, tags: Collection<String>) {
        val tagSet = ITEM_2_TAGS[identifier]
        if (tagSet != null) {
            tagSet.addAll(tags)
        } else ITEM_2_TAGS[identifier] = HashSet(tags)

        for (tag in tags) {
            val itemSet = TAG_2_ITEMS[tag]
            if (itemSet != null) {
                itemSet.add(identifier)
            } else TAG_2_ITEMS[tag] = HashSet(setOf(identifier))
        }
    }
}
