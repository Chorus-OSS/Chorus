package org.chorus_oss.chorus.registry

import com.google.gson.Gson
import org.chorus_oss.chorus.block.BlockAir
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.Item.Companion.get
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.nbt.NBTIO.read
import org.chorus_oss.chorus.network.protocol.types.inventory.creative.CreativeItemData
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.protocol.types.creative.CreativeCategory
import org.chorus_oss.protocol.types.creative.CreativeGroup
import org.chorus_oss.protocol.types.item.ItemInstance
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteOrder
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class CreativeItemRegistry : ItemID, IRegistry<Int, Item?, Item> {
    override fun init() {
        if (isLoad.getAndSet(true)) return

        try {
            CreativeItemRegistry::class.java.classLoader.getResourceAsStream("creative_items.json").use { input ->
                if (input == null) {
                    throw RuntimeException("Could not load creative_items.json")
                }
                val data = Gson().fromJson<Map<String, Any>>(
                    InputStreamReader(input),
                    MutableMap::class.java
                )
                val groups = data["groups"] as List<*>?
                for (i in groups!!.indices) {
                    val tag = groups[i] as Map<*, *>
                    val creativeCategory = (tag.getOrDefault("creative_category", 0) as Number).toInt()
                    val name = tag["name"] as String
                    val iconMap = tag["icon"] as Map<*, *>
                    val icon = get(iconMap["id"] as String)
                    creativeGroups.add(
                        CreativeGroup(
                            category = CreativeCategory.entries[creativeCategory],
                            name = name,
                            icon = ItemInstance(icon)
                        )
                    )
                }

                val items = data["items"] as List<*>?
                for (i in items!!.indices) {
                    val tag = items[i] as Map<*, *>
                    val damage = (tag.getOrDefault("damage", 0) as Number).toInt()
                    val groupIndex = (tag.getOrDefault("group_index", -1) as Number).toInt()
                    val nbt = if (tag.containsKey("nbt_b64")) Base64.getDecoder()
                        .decode(tag["nbt_b64"].toString()) else byteArrayOf()
                    val name = tag["id"].toString()
                    var item = get(name, damage, 1, nbt, false)
                    item.setCompoundTag(nbt)
                    if (ItemRegistry.itemComponents.containsCompound(name)) {
                        item.setNamedTag(ItemRegistry.itemComponents.getCompound(name).getCompound("components"))
                    }
                    if (item.isNothing || (item.isBlock() && item.getSafeBlockState() == BlockAir.properties.defaultState)) {
                        item = Item.AIR
                        log.warn("load creative item {} damage {} is null", name, damage)
                    }
                    val isBlock = tag.containsKey("block_state_b64")
                    if (isBlock) {
                        val blockTag = Base64.getDecoder().decode(tag["block_state_b64"].toString())
                        val blockCompoundTag = read(blockTag, ByteOrder.LITTLE_ENDIAN)
                        val blockHash = blockCompoundTag.getInt("network_id")
                        val block = Registries.BLOCKSTATE[blockHash]
                        if (block == null) {
                            item = Item.AIR
                            log.warn("load creative item {} blockHash {} is null", name, blockHash)
                        } else {
                            item.blockState = block
                            val updateDamage = block.toItem()
                            if (updateDamage.damage != 0) {
                                item.damage = updateDamage.damage
                            }
                        }
                    } else {
                        INTERNAL_DIFF_ITEM[i] = item.clone()
                        item.blockState = null
                    }
                    creativeItemData.add(
                        CreativeItemData(
                            item,
                            groupIndex
                        )
                    )

                    register(i, item)
                }
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: RegisterException) {
            throw RuntimeException(e)
        }
    }

    /**
     * 获取指定物品在[CreativeItemRegistry]中的索引
     *
     *
     * Get the index of the specified item in [CreativeItemRegistry]
     *
     * @param item 指定物品 <br></br>specified item
     * @return Unable to find return -1
     */
    fun getCreativeItemIndex(item: Item): Int {
        for (i in 0..<MAP.size) {
            if (item.equals(MAP[i]!!, !item.isTool)) {
                return i
            }
        }
        return -1
    }

    fun getCreativeItem(index: Int): Item? {
        if (INTERNAL_DIFF_ITEM.containsKey(index)) {
            return INTERNAL_DIFF_ITEM[index]
        }
        return if (index >= 0 && index < MAP.size) MAP[index] else Item.AIR
    }

    /**
     * 取消创造模式下创造背包中的物品
     *
     *
     * Cancel the Creative of items in the backpack in Creative mode
     */
    fun clearCreativeItems() {
        MAP.clear()
        INTERNAL_DIFF_ITEM.clear()
    }

    val creativeItems: Array<Item>
        /**
         * Get all creative items
         */
        get() = MAP.values.toTypedArray()

    /**
     * Add an item to [CreativeItemRegistry]
     */
    fun addCreativeItem(item: Item) {
        val i = MAP.size - 1
        try {
            this.register(i + 1, item.clone())
        } catch (e: RegisterException) {
            throw RuntimeException(e)
        }
    }

    /**
     * 移除一个指定的创造物品
     *
     *
     * Remove a specified created item
     */
    fun removeCreativeItem(item: Item) {
        val index = getCreativeItemIndex(item)
        if (index != -1) {
            val lastIntKey = MAP.size - 1
            for (i in index..<lastIntKey) {
                MAP[i] = MAP[i + 1]!!
            }
            MAP.remove(lastIntKey)
            INTERNAL_DIFF_ITEM.remove(index)
        }
    }

    /**
     * 检测这个物品是否存在于创造背包
     *
     *
     * Detect if the item exists in the Creative backpack
     */
    fun isCreativeItem(item: Item): Boolean {
        for (aCreative in INTERNAL_DIFF_ITEM.values) {
            if (item.equals(aCreative, !item.isTool)) {
                return true
            }
        }
        for (aCreative in MAP.values) {
            if (item.equals(aCreative, !item.isTool)) {
                return true
            }
        }
        return false
    }

    override operator fun get(key: Int): Item? {
        if (INTERNAL_DIFF_ITEM.containsKey(key)) {
            return INTERNAL_DIFF_ITEM[key]
        }
        return MAP[key]
    }

    override fun reload() {
        isLoad.set(false)
        MAP.clear()
        INTERNAL_DIFF_ITEM.clear()
        init()
    }

    @Throws(RegisterException::class)
    override fun register(key: Int, value: Item) {
        if (MAP.putIfAbsent(key, value) != null || creativeItemData.any { it.item == value }) {
            return
        } else {
            creativeItemData.add(CreativeItemData(value, 111))
        }
    }

    companion object : Loggable {
        val NETWORK_ITEMS: MutableList<Item> = mutableListOf()
        val MAP: MutableMap<Int, Item> = mutableMapOf()
        val INTERNAL_DIFF_ITEM: MutableMap<Int, Item> = mutableMapOf()
        val isLoad: AtomicBoolean = AtomicBoolean(false)

        val creativeGroups: MutableList<CreativeGroup> = mutableListOf()
        val creativeItemData: MutableList<CreativeItemData> = mutableListOf()
    }
}
