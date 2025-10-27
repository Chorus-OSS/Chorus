package org.chorus_oss.chorus.experimental.network.connection

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.encodeToByteString

class BedrockMessage(
    var edition: String = "MCPE",
    var name: String = "",
    var protocol: Int,
    var version: String = "",
    var playerCount: Int = -1,
    var playerMax: Int = -1,
    var guid: ULong = 0u,
    var subName: String = "",
    var gamemode: String = "",
    var nintendoLimited: Boolean = false,
    var port: Int? = 19132,
    var portV6: Int? = null,
    var extras: List<String> = emptyList(),
) {
    override fun toString(): String {
        return listOfNotNull(
            edition,
            name,
            protocol,
            version,
            playerCount,
            playerMax,
            guid,
            subName,
            gamemode,
            if (nintendoLimited) 0 else 1,
            port?.toString(),
            portV6?.toString(),
            extras.joinToString(separator = ";")
        ).joinToString(separator = ";", postfix = ";")
    }

    fun toByteString(): ByteString {
        return this.toString().encodeToByteString()
    }
}
