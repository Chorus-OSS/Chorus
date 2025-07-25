package org.chorus_oss.chorus.utils

import kotlinx.serialization.json.JsonObject
import java.util.*

interface LoginChainData {
    val username: String?

    val clientUUID: UUID?

    val identityPublicKey: String?

    val clientId: Long

    val serverAddress: String?

    val deviceModel: String?

    val deviceOS: Int

    val deviceId: String?

    val gameVersion: String?

    val guiScale: Int

    val languageCode: String?

    val xuid: String?

    val isXboxAuthed: Boolean

    val currentInputMode: Int

    val defaultInputMode: Int

    val capeData: String?

    val uIProfile: Int

    val waterdogXUID: String?

    val waterdogIP: String?

    val maxViewDistance: Int

    val memoryTier: Int

    val rawData: JsonObject?
}
