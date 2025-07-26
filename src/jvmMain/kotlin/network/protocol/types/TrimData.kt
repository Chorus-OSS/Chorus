package org.chorus_oss.chorus.network.protocol.types

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.chorus_oss.chorus.generated.resources.Res

object TrimData {
    var trimPatterns: List<TrimPattern> = emptyList()
    var trimMaterials: List<TrimMaterial> = emptyList()

    init {
        try {
            runBlocking { Res.readBytes("files/trim_data.json").inputStream() }.use { stream ->
                val obj = Json.parseToJsonElement(stream.reader().use { it.readText() }).jsonObject
                val l1 = mutableListOf<TrimPattern>()
                val l2 = mutableListOf<TrimMaterial>()
                for (e in obj["patterns"]!!.jsonArray) {
                    val asJsonObject = e.jsonObject
                    l1.add(
                        TrimPattern(
                            asJsonObject["itemName"]!!.jsonPrimitive.content,
                            asJsonObject["patternId"]!!.jsonPrimitive.content,
                        )
                    )
                }
                for (e in obj["materials"]!!.jsonArray) {
                    val asJsonObject = e.jsonObject
                    l2.add(
                        TrimMaterial(
                            asJsonObject["materialId"]!!.jsonPrimitive.content,
                            asJsonObject["color"]!!.jsonPrimitive.content,
                            asJsonObject["itemName"]!!.jsonPrimitive.content,
                        )
                    )
                }
                trimPatterns = l1.toList()
                trimMaterials = l2.toList()
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}
