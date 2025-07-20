package org.chorus_oss.chorus.level.updater.block


import kotlinx.serialization.json.*
import org.chorus_oss.chorus.level.updater.Updater
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagEditHelper
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagUpdaterContext
import java.io.IOException

class BlockStateUpdaterBase : Updater {
    override fun registerUpdaters(context: CompoundTagUpdaterContext) {
        context.addUpdater(0, 0, 0)
            .regex("name", "minecraft:.+")
            .regex("val", "[0-9]+")
            .addCompound("states")
            .tryEdit("states") { helper: CompoundTagEditHelper ->
                val tag = helper.compoundTag
                val parent = helper.parent
                val id = parent!!["name"] as String?
                var `val` = parent["val"] as Short
                val statesArray = LEGACY_BLOCK_DATA_MAP[id]
                if (statesArray != null) {
                    if (`val` >= statesArray.size) `val` = 0
                    tag.putAll(statesArray[`val`.toInt()])
                }
            }
            .remove("val")
    }

    companion object {
        @JvmField
        val INSTANCE: Updater = BlockStateUpdaterBase()

        val LEGACY_BLOCK_DATA_MAP: MutableMap<String, Array<Map<String, Any?>>> = HashMap()

        init {
            val node: kotlinx.serialization.json.JsonObject
            try {
                Updater::class.java.classLoader.getResourceAsStream("legacy_block_data_map.json").use { stream ->
                    checkNotNull(stream)
                    node = Json.parseToJsonElement(stream.reader().use { it.readText() }).jsonObject
                }
            } catch (e: IOException) {
                throw AssertionError("Error loading legacy block data map", e)
            }

            for (entry in node.entries) {
                val name: String = entry.key
                val stateNodes = entry.value.jsonArray

                val states = stateNodes.map { convertStateToCompound(it.jsonObject) }.toTypedArray()

                LEGACY_BLOCK_DATA_MAP[name] = states
            }
        }

        private fun convertStateToCompound(node: kotlinx.serialization.json.JsonObject): Map<String, Any?> {
            val tag: MutableMap<String, Any?> = LinkedHashMap()
            val iterator = node.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                val name: String = entry.key
                val value = entry.value
                if (value is kotlinx.serialization.json.JsonPrimitive) {
                    val primitive = entry.value.jsonPrimitive

                    tag[name] = primitive.booleanOrNull ?: primitive.intOrNull ?: primitive.contentOrNull ?: throw UnsupportedOperationException("Invalid state type")
                } else throw UnsupportedOperationException("Invalid state type")
            }
            return tag
        }
    }
}
