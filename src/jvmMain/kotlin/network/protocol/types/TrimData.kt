package org.chorus_oss.chorus.network.protocol.types

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.chorus_oss.chorus.generated.resources.Res

object TrimData {
    var patterns: List<TrimPattern> = emptyList()
    var materials: List<TrimMaterial> = emptyList()

    @Serializable
    private data class Data(
        val patterns: List<TrimPattern>,
        val materials: List<TrimMaterial>,
    )

    init {
        runBlocking {
            val json = Res.readBytes("files/trim_data.json").decodeToString()
            val data = Json.decodeFromString<Data>(json)
            patterns = data.patterns
            materials = data.materials
        }
    }
}
