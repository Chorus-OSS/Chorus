package org.chorus_oss.chorus.network.protocol.types

import kotlinx.serialization.Serializable

@JvmRecord
@Serializable
data class TrimPattern(
    val itemName: String,
    val patternId: String
)
