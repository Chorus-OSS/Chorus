package org.chorus_oss.chorus.network.protocol.types

import kotlinx.serialization.Serializable

@JvmRecord
@Serializable
data class TrimMaterial(
    val materialId: String,
    val color: String,
    val itemName: String
)
