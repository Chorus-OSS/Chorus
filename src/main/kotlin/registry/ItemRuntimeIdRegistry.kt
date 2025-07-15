package org.chorus_oss.chorus.registry

import com.google.gson.JsonParser
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.utils.BinaryStream
import org.chorus_oss.protocol.types.item.ItemInstance
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.atomic.AtomicBoolean

class ItemRuntimeIdRegistry : IRegistry<String, Int, Int> {
    private fun generatePalette() {
        val paletteBuffer = BinaryStream()
        val verify = HashMap<Int, Boolean?>()
        paletteBuffer.putUnsignedVarInt((REGISTRY.size + CUSTOM_REGISTRY.size).toLong())
        for (entry in REGISTRY.entries) {
            paletteBuffer.putString(entry.key)
            val rid = entry.value
            paletteBuffer.putLShort(rid)
            require(verify.putIfAbsent(rid, true) == null) { "Runtime ID is already registered: $rid" }
            paletteBuffer.putBoolean(false) //Vanilla Item doesnt component item
        }
        for ((key, value) in CUSTOM_REGISTRY.entries) {
            paletteBuffer.putString(key)
            val rid = value.runtimeId
            paletteBuffer.putLShort(rid)
            require(verify.putIfAbsent(rid, true) == null) { "Runtime ID is already registered: $rid" }
            paletteBuffer.putBoolean(value.isComponent)
        }
        itemPalette = paletteBuffer.getBufferCopy()
    }

    override fun init() {
        if (isLoad.getAndSet(true)) return

        // We use ProxyPass data since protocol 776 since we need item version and componentBased now.
        try {
            ItemRegistry::class.java.classLoader.getResourceAsStream("runtime_item_states.json").use { stream ->
                if (stream == null) {
                    throw RuntimeException("Failed to load runtime_item_states.json")
                }
                val items = JsonParser.parseReader(InputStreamReader(stream)).asJsonArray

                for (element in items) {
                    val item = element.asJsonObject
                    register1(
                        ItemData(
                            item["name"].asString,
                            item["id"].asInt,
                            item["version"].asInt,
                            item["componentBased"].asBoolean
                        )
                    )
                }
                generatePalette()
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    override fun get(key: String): Int {
        val i = REGISTRY[key]
        if (i == null) {
            val runtimeEntry = CUSTOM_REGISTRY[key]
            return runtimeEntry?.runtimeId ?: Int.MAX_VALUE
        }
        return i
    }

    fun getInt(key: String?): Int {
        val i = REGISTRY.get(key = key)
        if (i == null) {
            val runtimeEntry = CUSTOM_REGISTRY[key]
            return runtimeEntry?.runtimeId ?: Int.MAX_VALUE
        }
        return i
    }

    fun getIdentifier(runtimeId: Int): String {
        return ID2NAME[runtimeId] ?: throw RegisterException("Runtime ID not registered: $runtimeId")
    }

    override fun reload() {
        isLoad.set(false)
        REGISTRY.clear()
        CUSTOM_REGISTRY.clear()
        init()
    }

    @Throws(RegisterException::class)
    override fun register(key: String, value: Int) {
        if (REGISTRY.putIfAbsent(key, value) == null) {
            ID2NAME[value] = key
        } else {
            throw RegisterException("The item: " + key + "runtime id has been registered!")
        }
    }

    @Throws(RegisterException::class)
    fun registerCustomRuntimeItem(entry: RuntimeEntry) {
        if (CUSTOM_REGISTRY.putIfAbsent(entry.identifier, entry) == null) {
            ID2NAME[entry.runtimeId] = entry.identifier
            ITEM_DATA.add(ItemData(entry.identifier, entry.runtimeId, 1, entry.isComponent))
        } else {
            throw RegisterException("The item: " + entry.identifier + " runtime id has been registered!")
        }
    }

    private fun register0(key: String, value: Int) {
        if (REGISTRY.putIfAbsent(key, value) == null) {
            ID2NAME[value] = key
        }

        // TODO: Move this out
        if (key == ItemID.SHIELD) ItemInstance.ShieldID = value
    }

    private fun register1(entry: ItemData) {
        if (!ITEM_DATA.contains(entry)) {
            ITEM_DATA.add(entry)
            register0(entry.identifier, entry.runtimeId)
        }
    }

    @JvmRecord
    data class RuntimeEntry(val identifier: String, val runtimeId: Int, val isComponent: Boolean)

    @JvmRecord
    data class ItemData(val identifier: String, val runtimeId: Int, val version: Int, val componentBased: Boolean)
    companion object {
        val isLoad = AtomicBoolean(false)

        val REGISTRY = HashMap<String, Int>()
        val CUSTOM_REGISTRY = HashMap<String, RuntimeEntry>()

        val ID2NAME = HashMap<Int, String>()


        val ITEM_DATA = HashSet<ItemData>()

        private lateinit var itemPalette: ByteArray
    }
}
